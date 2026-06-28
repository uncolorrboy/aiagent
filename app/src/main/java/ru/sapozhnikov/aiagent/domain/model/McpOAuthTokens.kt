package ru.sapozhnikov.aiagent.domain.model

/** OAuth-токены, полученные при авторизации MCP-сервера. */
internal data class McpOAuthTokens(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAtEpochSeconds: Long? = null,
    val tokenType: String = "Bearer",
) {
    fun isExpired(clockEpochSeconds: Long = System.currentTimeMillis() / 1000): Boolean {
        val expiresAt = expiresAtEpochSeconds ?: return false
        return clockEpochSeconds >= expiresAt - 60
    }
}
