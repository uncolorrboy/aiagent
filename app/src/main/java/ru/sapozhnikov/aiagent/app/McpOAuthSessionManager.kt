package ru.sapozhnikov.aiagent.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.sapozhnikov.aiagent.data.remote.mcp.oauth.McpOAuthAuthorizationRequest
import ru.sapozhnikov.aiagent.data.remote.mcp.oauth.McpOAuthService
import ru.sapozhnikov.aiagent.domain.model.McpOAuthClientInfo
import ru.sapozhnikov.aiagent.domain.model.McpOAuthDiscoveryState
import ru.sapozhnikov.aiagent.domain.model.McpOAuthTokens
import ru.sapozhnikov.aiagent.domain.model.McpServerConfig
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Состояние активной OAuth-сессии для MCP-сервера. */
internal data class McpOAuthPendingSession(
    val serverId: String,
    val codeVerifier: String,
    val state: String,
    val redirectUri: String,
    val discoveryState: McpOAuthDiscoveryState,
    val clientInfo: McpOAuthClientInfo,
    val resourceUrl: String,
    val scope: String?,
)

/** Результат завершения OAuth-авторизации. */
internal sealed interface McpOAuthCompletionEvent {
    data class Success(val serverId: String) : McpOAuthCompletionEvent
    data class Failure(val serverId: String, val message: String) : McpOAuthCompletionEvent
}

@Singleton
internal class McpOAuthSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mcpOAuthService: McpOAuthService,
    private val settingsRepository: SettingsRepository,
) {
    private val pendingSessions = mutableMapOf<String, McpOAuthPendingSession>()
    private val completionDeferreds = mutableMapOf<String, CompletableDeferred<Result<McpOAuthTokens>>>()
    private val _completionEvents = MutableSharedFlow<McpOAuthCompletionEvent>(extraBufferCapacity = 1)
    val completionEvents = _completionEvents.asSharedFlow()

    fun redirectUri(): String = REDIRECT_URI

    suspend fun startAuthorization(server: McpServerConfig): McpOAuthAuthorizationRequest {
        val request = mcpOAuthService.buildAuthorizationRequest(
            serverUrl = server.url,
            redirectUri = REDIRECT_URI,
            existingClientInfo = server.oauthClientInfo,
            existingDiscovery = server.oauthDiscovery,
        )

        pendingSessions[request.state] = McpOAuthPendingSession(
            serverId = server.id,
            codeVerifier = request.codeVerifier,
            state = request.state,
            redirectUri = REDIRECT_URI,
            discoveryState = request.discoveryState,
            clientInfo = request.clientInfo,
            resourceUrl = request.resourceUrl,
            scope = request.scope,
        )

        settingsRepository.updateMcpServer(
            server.copy(
                oauthClientInfo = request.clientInfo,
                oauthDiscovery = request.discoveryState,
            ),
        )

        return request
    }

    fun openAuthorizationPage(authorizationUrl: String) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(context, Uri.parse(authorizationUrl))
    }

    suspend fun awaitAuthorization(serverId: String): Result<McpOAuthTokens> {
        val deferred = CompletableDeferred<Result<McpOAuthTokens>>()
        completionDeferreds[serverId] = deferred
        return deferred.await()
    }

    suspend fun handleCallback(uri: Uri): Boolean {
        val code = uri.getQueryParameter("code") ?: return false
        val state = uri.getQueryParameter("state") ?: return false
        val error = uri.getQueryParameter("error")
        val session = pendingSessions.remove(state) ?: return false

        if (error != null) {
            val message = uri.getQueryParameter("error_description") ?: error
            completeSession(session.serverId, Result.failure(IllegalStateException(message)))
            _completionEvents.tryEmit(McpOAuthCompletionEvent.Failure(session.serverId, message))
            return true
        }

        return runCatching {
            mcpOAuthService.exchangeAuthorizationCode(
                authorizationCode = code,
                codeVerifier = session.codeVerifier,
                redirectUri = session.redirectUri,
                discoveryState = session.discoveryState,
                clientInfo = session.clientInfo,
                resourceUrl = session.resourceUrl,
                scope = session.scope,
            )
        }.fold(
            onSuccess = { tokens ->
                val server = settingsRepository.getMcpServers()
                    .firstOrNull { it.id == session.serverId }
                if (server != null) {
                    settingsRepository.updateMcpServer(
                        server.copy(
                            oauthTokens = tokens,
                            oauthClientInfo = session.clientInfo,
                            oauthDiscovery = session.discoveryState,
                        ),
                    )
                }
                completeSession(session.serverId, Result.success(tokens))
                _completionEvents.tryEmit(McpOAuthCompletionEvent.Success(session.serverId))
                true
            },
            onFailure = { error ->
                completeSession(
                    session.serverId,
                    Result.failure(error),
                )
                _completionEvents.tryEmit(
                    McpOAuthCompletionEvent.Failure(
                        serverId = session.serverId,
                        message = error.message ?: "OAuth authorization failed",
                    ),
                )
                true
            },
        )
    }

    suspend fun refreshTokensIfNeeded(server: McpServerConfig): McpServerConfig {
        val tokens = server.oauthTokens ?: return server
        if (!tokens.isExpired()) return server
        val refreshToken = tokens.refreshToken ?: return server
        val discovery = server.oauthDiscovery ?: return server
        val clientInfo = server.oauthClientInfo ?: return server
        val resourceUrl = mcpOAuthService.resolveResourceUrl(server.url)

        val refreshed = mcpOAuthService.refreshTokens(
            refreshToken = refreshToken,
            discoveryState = discovery,
            clientInfo = clientInfo,
            resourceUrl = resourceUrl,
        )
        val updated = server.copy(oauthTokens = refreshed)
        settingsRepository.updateMcpServer(updated)
        return updated
    }

    private fun completeSession(serverId: String, result: Result<McpOAuthTokens>) {
        completionDeferreds.remove(serverId)?.complete(result)
    }

    private companion object {
        const val REDIRECT_URI = "ru.sapozhnikov.aiagent://oauth/callback"
    }
}
