package ru.sapozhnikov.aiagent.domain.model

/** Способ авторизации при подключении к MCP-серверу. */
internal enum class McpAuthType {
    /** Без авторизации. */
    NONE,

    /** Статический Bearer-токен, вводится вручную. */
    BEARER,

    /** OAuth 2.1 по спецификации MCP (PKCE + discovery). */
    OAUTH,
}
