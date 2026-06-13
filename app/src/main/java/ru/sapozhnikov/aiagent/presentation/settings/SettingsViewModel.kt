package ru.sapozhnikov.aiagent.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.SettingsInteractor
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
            settingsInteractor.observeContextManagementEnabled().collect { enabled ->
                _uiState.update { it.copy(contextManagementEnabled = enabled) }
            }
        }
    }

    /**
     * Обрабатывает изменение флага управления контекстом.
     *
     * @param enabled включено ли сжатие контекста через резюме
     */
    fun onContextManagementChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsInteractor.setContextManagementEnabled(enabled)
        }
    }
}
