package ru.sapozhnikov.aiagent.domain.model

/**
 * Конфигурация MCP-сервера, сохранённая в настройках.
 *
 * @property id уникальный идентификатор сервера
 * @property url URL MCP-сервера
 * @property authType способ авторизации
 * @property authToken Bearer-токен для [McpAuthType.BEARER]
 * @property oauthTokens OAuth-токены для [McpAuthType.OAUTH]
 * @property oauthClientInfo зарегистрированный OAuth-клиент
 * @property oauthDiscovery сохранённое состояние OAuth discovery
 * @property enabled участвует ли сервер в работе агента
 * @property serverName имя сервера после последнего успешного подключения
 * @property serverVersion версия сервера после последнего успешного подключения
 * @property tools список инструментов после последнего успешного подключения
 */
internal data class McpServerConfig(
    val id: String,
    val url: String,
    val authType: McpAuthType = McpAuthType.NONE,
    val authToken: String = "",
    val oauthTokens: McpOAuthTokens? = null,
    val oauthClientInfo: McpOAuthClientInfo? = null,
    val oauthDiscovery: McpOAuthDiscoveryState? = null,
    val enabled: Boolean = true,
    val serverName: String? = null,
    val serverVersion: String? = null,
    val tools: List<McpToolDefinition> = emptyList(),
) {
    fun resolveAccessToken(): String? = when (authType) {
        McpAuthType.NONE -> null
        McpAuthType.BEARER -> authToken.trim().takeIf { it.isNotEmpty() }
        McpAuthType.OAUTH -> oauthTokens?.accessToken?.takeIf { it.isNotEmpty() }
    }
}
