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

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsInteractor.observeContextManagementEnabled().collect { enabled ->
                _uiState.update { it.copy(contextManagementEnabled = enabled) }
            }
        }
    }

    fun onContextManagementChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsInteractor.setContextManagementEnabled(enabled)
        }
    }
}
