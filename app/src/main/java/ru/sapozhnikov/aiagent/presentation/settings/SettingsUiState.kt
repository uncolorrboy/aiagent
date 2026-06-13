package ru.sapozhnikov.aiagent.presentation.settings

/**
 * Состояние UI экрана настроек.
 *
 * @property contextManagementEnabled включено ли сжатие контекста через резюме
 */
internal data class SettingsUiState(
    val contextManagementEnabled: Boolean = false,
)
