package ru.sapozhnikov.aiagent.domain.model

/** Сохранённое состояние OAuth discovery для повторного использования. */
internal data class McpOAuthDiscoveryState(
    val authorizationServerUrl: String,
    val resourceMetadataUrl: String? = null,
    val authorizationEndpoint: String? = null,
    val tokenEndpoint: String? = null,
    val registrationEndpoint: String? = null,
    val issuer: String? = null,
    val scopesSupported: List<String> = emptyList(),
)
