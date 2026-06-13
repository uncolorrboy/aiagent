package ru.sapozhnikov.aiagent.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.SettingsInteractor
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import javax.inject.Inject

/**
 * ViewModel экрана настроек.
 *
 * @param settingsInteractor use-case для чтения и записи настроек
 */
@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    /** Состояние UI экрана настроек. */
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsInteractor.observeContextManagementStrategy().collect { strategy ->
                _uiState.update { it.copy(contextManagementStrategy = strategy) }
            }
        }
    }

    /**
     * Обрабатывает выбор стратегии управления контекстом.
     *
     * @param strategy новая стратегия
     */
    fun onContextManagementStrategyChanged(strategy: ContextManagementStrategy) {
        viewModelScope.launch {
            settingsInteractor.setContextManagementStrategy(strategy)
        }
    }
}
