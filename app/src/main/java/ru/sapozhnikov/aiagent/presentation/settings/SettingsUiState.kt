package ru.sapozhnikov.aiagent.presentation.settings

import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy

/**
 * Состояние UI экрана настроек.
 *
 * @property contextManagementStrategy выбранная стратегия управления контекстом
 */
internal data class SettingsUiState(
    val contextManagementStrategy: ContextManagementStrategy = ContextManagementStrategy.DEFAULT,
)
