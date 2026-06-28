package ru.sapozhnikov.aiagent.presentation.settings

import ru.sapozhnikov.aiagent.domain.model.McpServerConfig
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition

/** Статус подключения к MCP-серверу. */
internal enum class McpConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    AUTHORIZING,
    CONNECTED,
    ERROR,
}

/** Элемент списка MCP-серверов на экране управления. */
internal data class McpServerItemUiState(
    val server: McpServerConfig,
    val connectionStatus: McpConnectionStatus = McpConnectionStatus.DISCONNECTED,
    val connectionError: String? = null,
    val toolsExpanded: Boolean = false,
) {
    val displayTitle: String
        get() = server.serverName ?: server.url

    val tools: List<McpToolDefinition>
        get() = server.tools
}

/** Состояние UI экрана управления MCP-серверами. */
internal data class McpServersUiState(
    val mcpEnabled: Boolean = false,
    val servers: List<McpServerItemUiState> = emptyList(),
) {
    val connectedCount: Int
        get() = servers.count { it.connectionStatus == McpConnectionStatus.CONNECTED }
}
