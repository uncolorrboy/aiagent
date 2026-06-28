package ru.sapozhnikov.aiagent.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.InvariantInteractor
import ru.sapozhnikov.aiagent.domain.interactor.MemoryInteractor
import ru.sapozhnikov.aiagent.domain.interactor.SettingsInteractor
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import javax.inject.Inject

/**
 * ViewModel экрана настроек.
 *
 * @param settingsInteractor use-case для чтения и записи настроек
 * @param memoryInteractor use-case для управления памятью ассистента
 */
@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val memoryInteractor: MemoryInteractor,
    private val invariantInteractor: InvariantInteractor,
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
        viewModelScope.launch {
            combine(
                memoryInteractor.observeWorkingMemoryInstances(),
                memoryInteractor.observeProfileMemoryInstances(),
            ) { working, profile ->
                working to profile
            }.collect { (working, profile) ->
                _uiState.update {
                    it.copy(
                        workingMemoryInstances = working,
                        profileMemoryInstances = profile,
                    )
                }
            }
        }
        viewModelScope.launch {
            invariantInteractor.observeAllInvariants().collect { invariants ->
                _uiState.update { it.copy(invariants = invariants) }
            }
        }
        viewModelScope.launch {
            combine(
                settingsInteractor.observeMcpEnabled(),
                settingsInteractor.observeMcpServers(),
            ) { mcpEnabled, servers ->
                Triple(
                    mcpEnabled,
                    servers.size,
                    servers.count { it.serverName != null },
                )
            }.collect { (mcpEnabled, serverCount, connectedCount) ->
                _uiState.update {
                    it.copy(
                        mcpEnabled = mcpEnabled,
                        mcpServerCount = serverCount,
                        mcpConnectedCount = connectedCount,
                    )
                }
            }
        }
    }

    /** Обрабатывает выбор стратегии управления контекстом. */
    fun onContextManagementStrategyChanged(strategy: ContextManagementStrategy) {
        viewModelScope.launch {
            settingsInteractor.setContextManagementStrategy(strategy)
        }
    }

    /** Удаляет экземпляр рабочей памяти. */
    fun onDeleteWorkingMemory(id: String) {
        viewModelScope.launch {
            memoryInteractor.deleteWorkingMemoryInstance(id)
        }
    }

    /** Удаляет экземпляр профиля. */
    fun onDeleteProfileMemory(id: String) {
        viewModelScope.launch {
            memoryInteractor.deleteProfileMemoryInstance(id)
        }
    }

    fun onDeleteInvariant(id: String) {
        viewModelScope.launch {
            invariantInteractor.deleteInvariant(id)
        }
    }

    fun onMcpEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsInteractor.setMcpEnabled(enabled)
        }
    }
}
