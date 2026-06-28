package ru.sapozhnikov.aiagent.data.remote.mcp.oauth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OAuthProtectedResourceMetadata(
    val resource: String? = null,
    @SerialName("authorization_servers")
    val authorizationServers: List<String> = emptyList(),
    @SerialName("scopes_supported")
    val scopesSupported: List<String>? = null,
)

@Serializable
internal data class AuthorizationServerMetadata(
    val issuer: String? = null,
    @SerialName("authorization_endpoint")
    val authorizationEndpoint: String? = null,
    @SerialName("token_endpoint")
    val tokenEndpoint: String? = null,
    @SerialName("registration_endpoint")
    val registrationEndpoint: String? = null,
    @SerialName("response_types_supported")
    val responseTypesSupported: List<String>? = null,
    @SerialName("code_challenge_methods_supported")
    val codeChallengeMethodsSupported: List<String>? = null,
    @SerialName("token_endpoint_auth_methods_supported")
    val tokenEndpointAuthMethodsSupported: List<String>? = null,
    @SerialName("scopes_supported")
    val scopesSupported: List<String>? = null,
)

@Serializable
internal data class DynamicClientRegistrationRequest(
    @SerialName("client_name")
    val clientName: String,
    @SerialName("redirect_uris")
    val redirectUris: List<String>,
    @SerialName("grant_types")
    val grantTypes: List<String> = listOf("authorization_code", "refresh_token"),
    @SerialName("response_types")
    val responseTypes: List<String> = listOf("code"),
    @SerialName("token_endpoint_auth_method")
    val tokenEndpointAuthMethod: String = "none",
    val scope: String? = null,
)

@Serializable
internal data class DynamicClientRegistrationResponse(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("client_secret")
    val clientSecret: String? = null,
)

@Serializable
internal data class OAuthTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("expires_in")
    val expiresIn: Long? = null,
    @SerialName("token_type")
    val tokenType: String? = null,
)

@Serializable
internal data class OAuthErrorResponse(
    val error: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null,
)
