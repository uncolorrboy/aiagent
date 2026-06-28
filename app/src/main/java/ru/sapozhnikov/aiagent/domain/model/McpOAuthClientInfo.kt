package ru.sapozhnikov.aiagent.domain.model

/** Зарегистрированные OAuth-клиентские данные для MCP-сервера. */
internal data class McpOAuthClientInfo(
    val clientId: String,
    val clientSecret: String? = null,
    val issuer: String? = null,
)
