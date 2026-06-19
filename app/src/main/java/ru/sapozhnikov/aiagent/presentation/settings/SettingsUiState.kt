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
 */
internal data class SettingsUiState(
    val contextManagementStrategy: ContextManagementStrategy = ContextManagementStrategy.DEFAULT,
    val workingMemoryInstances: List<MemoryInstance> = emptyList(),
    val profileMemoryInstances: List<MemoryInstance> = emptyList(),
    val invariants: List<AssistantInvariant> = emptyList(),
)
