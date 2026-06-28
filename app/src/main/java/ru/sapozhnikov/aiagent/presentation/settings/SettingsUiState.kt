package ru.sapozhnikov.aiagent.presentation.settings

import ru.sapozhnikov.aiagent.domain.model.AssistantInvariant
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance

/**
 * Состояние UI экрана настроек.
 *
 * @property contextManagementStrategy выбранная стратегия управления контекстом
 * @property workingMemoryInstances экземпляры рабочей памяти
 * @property profileMemoryInstances экземпляры долговременной памяти
 * @property invariants список инвариантов ассистента
 * @property mcpEnabled включён ли MCP
 * @property mcpServerCount количество настроенных MCP-серверов
 * @property mcpConnectedCount количество подключённых MCP-серверов
 */
internal data class SettingsUiState(
    val contextManagementStrategy: ContextManagementStrategy = ContextManagementStrategy.DEFAULT,
    val workingMemoryInstances: List<MemoryInstance> = emptyList(),
    val profileMemoryInstances: List<MemoryInstance> = emptyList(),
    val invariants: List<AssistantInvariant> = emptyList(),
    val mcpEnabled: Boolean = false,
    val mcpServerCount: Int = 0,
    val mcpConnectedCount: Int = 0,
)
