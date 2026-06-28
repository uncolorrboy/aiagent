package ru.sapozhnikov.aiagent.data.remote.mcp.oauth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.sapozhnikov.aiagent.domain.model.McpOAuthClientInfo
import ru.sapozhnikov.aiagent.domain.model.McpOAuthDiscoveryState
import ru.sapozhnikov.aiagent.domain.model.McpOAuthTokens
import java.net.URI
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Результат запуска OAuth-авторизации — требуется открыть браузер. */
internal data class McpOAuthAuthorizationRequest(
    val authorizationUrl: String,
    val codeVerifier: String,
    val state: String,
    val discoveryState: McpOAuthDiscoveryState,
    val clientInfo: McpOAuthClientInfo,
    val resourceUrl: String,
    val scope: String?,
)

@Singleton
internal class McpOAuthService @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun buildAuthorizationRequest(
        serverUrl: String,
        redirectUri: String,
        existingClientInfo: McpOAuthClientInfo?,
        existingDiscovery: McpOAuthDiscoveryState?,
    ): McpOAuthAuthorizationRequest {
        val serverUri = URI(serverUrl.trim())
        val resourceMetadataUrl = probeResourceMetadataUrl(serverUrl)
        val protectedResourceMetadata = discoverProtectedResourceMetadata(
            serverUrl = serverUrl,
            resourceMetadataUrl = resourceMetadataUrl,
        )
        val authorizationServerUrl = protectedResourceMetadata.authorizationServers.firstOrNull()
            ?: throw IllegalStateException("MCP-сервер не указал authorization server")

        val authServerMetadata = discoverAuthorizationServerMetadata(
            authorizationServerUrl = authorizationServerUrl,
            cachedDiscovery = existingDiscovery,
        )
        val issuer = authServerMetadata.issuer ?: authorizationServerUrl
        val discoveryState = McpOAuthDiscoveryState(
            authorizationServerUrl = authorizationServerUrl,
            resourceMetadataUrl = resourceMetadataUrl,
            authorizationEndpoint = authServerMetadata.authorizationEndpoint,
            tokenEndpoint = authServerMetadata.tokenEndpoint,
            registrationEndpoint = authServerMetadata.registrationEndpoint,
            issuer = issuer,
            scopesSupported = authServerMetadata.scopesSupported.orEmpty(),
        )

        val clientInfo = existingClientInfo?.takeIf { it.issuer == null || it.issuer == issuer }
            ?: registerClient(
                discoveryState = discoveryState,
                redirectUri = redirectUri,
                scope = determineScope(protectedResourceMetadata, authServerMetadata),
            )

        val resourceUrl = selectResourceUrl(serverUrl, protectedResourceMetadata.resource)
        val scope = determineScope(protectedResourceMetadata, authServerMetadata)
        val state = UUID.randomUUID().toString()
        val pkce = McpPkce.createChallenge()
        val authorizationEndpoint = authServerMetadata.authorizationEndpoint
            ?: throw IllegalStateException("Authorization server не предоставил authorization_endpoint")

        val authorizationUrl = URLBuilder(authorizationEndpoint).apply {
            parameters.append("response_type", "code")
            parameters.append("client_id", clientInfo.clientId)
            parameters.append("redirect_uri", redirectUri)
            parameters.append("code_challenge", pkce.codeChallenge)
            parameters.append("code_challenge_method", pkce.codeChallengeMethod)
            parameters.append("state", state)
            parameters.append("resource", resourceUrl)
            scope?.let { parameters.append("scope", it) }
            if (scope?.split(' ')?.contains("offline_access") == true) {
                parameters.append("prompt", "consent")
            }
        }.buildString()

        return McpOAuthAuthorizationRequest(
            authorizationUrl = authorizationUrl,
            codeVerifier = pkce.codeVerifier,
            state = state,
            discoveryState = discoveryState,
            clientInfo = clientInfo.copy(issuer = issuer),
            resourceUrl = resourceUrl,
            scope = scope,
        )
    }

    suspend fun exchangeAuthorizationCode(
        authorizationCode: String,
        codeVerifier: String,
        redirectUri: String,
        discoveryState: McpOAuthDiscoveryState,
        clientInfo: McpOAuthClientInfo,
        resourceUrl: String,
        scope: String?,
    ): McpOAuthTokens {
        val tokenEndpoint = discoveryState.tokenEndpoint
            ?: throw IllegalStateException("Authorization server не предоставил token_endpoint")

        val params = Parameters.build {
            append("grant_type", "authorization_code")
            append("code", authorizationCode)
            append("code_verifier", codeVerifier)
            append("redirect_uri", redirectUri)
            append("resource", resourceUrl)
            scope?.let { append("scope", it) }
        }

        return requestTokens(
            tokenEndpoint = tokenEndpoint,
            params = params,
            clientInfo = clientInfo,
            authMethods = emptyList(),
        )
    }

    suspend fun refreshTokens(
        refreshToken: String,
        discoveryState: McpOAuthDiscoveryState,
        clientInfo: McpOAuthClientInfo,
        resourceUrl: String,
    ): McpOAuthTokens {
        val tokenEndpoint = discoveryState.tokenEndpoint
            ?: throw IllegalStateException("Authorization server не предоставил token_endpoint")

        val params = Parameters.build {
            append("grant_type", "refresh_token")
            append("refresh_token", refreshToken)
            append("resource", resourceUrl)
        }

        val tokens = requestTokens(
            tokenEndpoint = tokenEndpoint,
            params = params,
            clientInfo = clientInfo,
            authMethods = emptyList(),
        )
        return tokens.copy(refreshToken = tokens.refreshToken ?: refreshToken)
    }

    private suspend fun probeResourceMetadataUrl(serverUrl: String): String? {
        val response = runCatching {
            httpClient.get(serverUrl.trim()) {
                header(HttpHeaders.Accept, "application/json")
            }
        }.getOrNull() ?: return null

        if (response.status != HttpStatusCode.Unauthorized) {
            return null
        }

        val wwwAuthenticate = response.headers[HttpHeaders.WWWAuthenticate] ?: return null
        return parseWwwAuthenticateParam(wwwAuthenticate, "resource_metadata")
    }

    private suspend fun discoverProtectedResourceMetadata(
        serverUrl: String,
        resourceMetadataUrl: String?,
    ): OAuthProtectedResourceMetadata {
        val candidates = buildList {
            resourceMetadataUrl?.let { add(it) }
            add(buildWellKnownUrl(serverUrl, "oauth-protected-resource", prependPath = true))
            add(buildWellKnownUrl(serverUrl, "oauth-protected-resource", prependPath = false))
        }.distinct()

        var lastError: Throwable? = null
        for (candidate in candidates) {
            val response = runCatching {
                httpClient.get(candidate) {
                    header(HttpHeaders.Accept, "application/json")
                }
            }.getOrElse { error ->
                lastError = error
                continue
            }

            if (response.status.value == 404) continue
            if (!response.status.isSuccess()) {
                lastError = IllegalStateException("HTTP ${response.status.value} при загрузке PRM")
                continue
            }

            return response.body<OAuthProtectedResourceMetadata>()
        }

        throw lastError ?: IllegalStateException("MCP-сервер не поддерживает OAuth Protected Resource Metadata")
    }

    private suspend fun discoverAuthorizationServerMetadata(
        authorizationServerUrl: String,
        cachedDiscovery: McpOAuthDiscoveryState?,
    ): AuthorizationServerMetadata {
        if (cachedDiscovery?.authorizationEndpoint != null && cachedDiscovery.tokenEndpoint != null) {
            return AuthorizationServerMetadata(
                issuer = cachedDiscovery.issuer,
                authorizationEndpoint = cachedDiscovery.authorizationEndpoint,
                tokenEndpoint = cachedDiscovery.tokenEndpoint,
                registrationEndpoint = cachedDiscovery.registrationEndpoint,
                scopesSupported = cachedDiscovery.scopesSupported,
                responseTypesSupported = listOf("code"),
                codeChallengeMethodsSupported = listOf("S256"),
            )
        }

        val candidates = listOf(
            buildWellKnownUrl(authorizationServerUrl, "oauth-authorization-server", prependPath = true),
            buildWellKnownUrl(authorizationServerUrl, "oauth-authorization-server", prependPath = false),
            buildWellKnownUrl(authorizationServerUrl, "openid-configuration", prependPath = true),
            buildWellKnownUrl(authorizationServerUrl, "openid-configuration", prependPath = false),
        ).distinct()

        var lastError: Throwable? = null
        for (candidate in candidates) {
            val response = runCatching {
                httpClient.get(candidate) {
                    header(HttpHeaders.Accept, "application/json")
                }
            }.getOrElse { error ->
                lastError = error
                continue
            }

            if (response.status.value == 404) continue
            if (!response.status.isSuccess()) {
                lastError = IllegalStateException("HTTP ${response.status.value} при загрузке AS metadata")
                continue
            }

            return response.body<AuthorizationServerMetadata>()
        }

        throw lastError ?: IllegalStateException("Authorization server metadata не найдена")
    }

    private suspend fun registerClient(
        discoveryState: McpOAuthDiscoveryState,
        redirectUri: String,
        scope: String?,
    ): McpOAuthClientInfo {
        val registrationEndpoint = discoveryState.registrationEndpoint
            ?: throw IllegalStateException(
                "Authorization server не поддерживает Dynamic Client Registration. " +
                    "Укажите client_id вручную или используйте Bearer-токен.",
            )

        val request = DynamicClientRegistrationRequest(
            clientName = "AiAgent Android",
            redirectUris = listOf(redirectUri),
            scope = scope,
        )

        val response = httpClient.post(registrationEndpoint) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.bodyAsText() }.getOrDefault("")
            throw IllegalStateException("Не удалось зарегистрировать OAuth-клиент: HTTP ${response.status.value} $body")
        }

        val registration = response.body<DynamicClientRegistrationResponse>()
        return McpOAuthClientInfo(
            clientId = registration.clientId,
            clientSecret = registration.clientSecret,
            issuer = discoveryState.issuer,
        )
    }

    private suspend fun requestTokens(
        tokenEndpoint: String,
        params: Parameters,
        clientInfo: McpOAuthClientInfo,
        authMethods: List<String>,
    ): McpOAuthTokens {
        val bodyParams = Parameters.build {
            params.entries().forEach { (key, values) ->
                values.forEach { value -> append(key, value) }
            }
            if (clientInfo.clientSecret.isNullOrBlank()) {
                append("client_id", clientInfo.clientId)
            }
        }

        val response = httpClient.post(tokenEndpoint) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(bodyParams.formUrlEncode())
            if (!clientInfo.clientSecret.isNullOrBlank()) {
                val credentials = "${clientInfo.clientId}:${clientInfo.clientSecret}"
                val encoded = android.util.Base64.encodeToString(
                    credentials.toByteArray(Charsets.UTF_8),
                    android.util.Base64.NO_WRAP,
                )
                header(HttpHeaders.Authorization, "Basic $encoded")
            }
        }

        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.body<OAuthErrorResponse>() }.getOrNull()
            val message = errorBody?.errorDescription ?: errorBody?.error ?: response.bodyAsText()
            throw IllegalStateException("OAuth token request failed: $message")
        }

        val tokenResponse = response.body<OAuthTokenResponse>()
        val expiresAt = tokenResponse.expiresIn?.let { seconds ->
            System.currentTimeMillis() / 1000 + seconds
        }
        return McpOAuthTokens(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            expiresAtEpochSeconds = expiresAt,
            tokenType = tokenResponse.tokenType ?: "Bearer",
        )
    }

    private fun determineScope(
        protectedResourceMetadata: OAuthProtectedResourceMetadata,
        authServerMetadata: AuthorizationServerMetadata,
    ): String? {
        val resourceScopes = protectedResourceMetadata.scopesSupported
        if (!resourceScopes.isNullOrEmpty()) {
            return resourceScopes.joinToString(" ")
        }
        val authScopes = authServerMetadata.scopesSupported
        if (!authScopes.isNullOrEmpty()) {
            return authScopes.joinToString(" ")
        }
        return null
    }

    fun resolveResourceUrl(serverUrl: String, resourceFromMetadata: String? = null): String {
        resourceFromMetadata?.takeIf { it.isNotBlank() }?.let { return normalizeResourceUrl(it) }
        return normalizeResourceUrl(serverUrl)
    }

    private fun selectResourceUrl(serverUrl: String, resourceFromMetadata: String?): String {
        return resolveResourceUrl(serverUrl, resourceFromMetadata)
    }

    private fun normalizeResourceUrl(url: String): String {
        val uri = URI(url.trim())
        val normalized = buildString {
            append(uri.scheme.lowercase())
            append("://")
            append(uri.host.lowercase())
            if (uri.port != -1) {
                append(':')
                append(uri.port)
            }
            append(uri.path.removeSuffix("/"))
        }
        return normalized
    }

    private fun buildWellKnownUrl(
        baseUrl: String,
        wellKnownType: String,
        prependPath: Boolean,
    ): String {
        val uri = URI(baseUrl.trim())
        val path = uri.path.orEmpty().removeSuffix("/")
        val wellKnownPath = if (prependPath && path.isNotEmpty()) {
            "$path/.well-known/$wellKnownType"
        } else {
            "/.well-known/$wellKnownType"
        }
        return URL(uri.scheme, uri.host, uri.port, wellKnownPath).toString()
    }

    private fun parseWwwAuthenticateParam(header: String, paramName: String): String? {
        val regex = Regex("""$paramName="([^"]+)"""")
        return regex.find(header)?.groupValues?.getOrNull(1)
    }
}
