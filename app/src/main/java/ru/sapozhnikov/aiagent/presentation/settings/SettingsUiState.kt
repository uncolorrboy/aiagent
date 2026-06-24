package ru.sapozhnikov.aiagent.presentation.settings

import ru.sapozhnikov.aiagent.domain.model.AssistantInvariant
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance

/** Статус подключения к MCP-серверу. */
internal enum class McpConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR,
}

/**
 * Состояние UI экрана настроек.
 *
 * @property contextManagementStrategy выбранная стратегия управления контекстом
 * @property workingMemoryInstances экземпляры рабочей памяти
 * @property profileMemoryInstances экземпляры долговременной памяти
 * @property invariants список инвариантов ассистента
 * @property mcpEnabled включён ли MCP
 * @property mcpServerUrl URL MCP-сервера
 * @property mcpAuthToken опциональный Bearer-токен
 * @property mcpConnectionStatus текущий статус подключения
 * @property mcpServerName имя подключённого сервера
 * @property mcpServerVersion версия подключённого сервера
 * @property mcpTools список инструментов сервера
 * @property mcpConnectionError текст ошибки подключения
 */
internal data class SettingsUiState(
    val contextManagementStrategy: ContextManagementStrategy = ContextManagementStrategy.DEFAULT,
    val workingMemoryInstances: List<MemoryInstance> = emptyList(),
    val profileMemoryInstances: List<MemoryInstance> = emptyList(),
    val invariants: List<AssistantInvariant> = emptyList(),
    val mcpEnabled: Boolean = false,
    val mcpServerUrl: String = "",
    val mcpAuthToken: String = "",
    val mcpConnectionStatus: McpConnectionStatus = McpConnectionStatus.DISCONNECTED,
    val mcpServerName: String? = null,
    val mcpServerVersion: String? = null,
    val mcpTools: List<McpToolDefinition> = emptyList(),
    val mcpConnectionError: String? = null,
)
