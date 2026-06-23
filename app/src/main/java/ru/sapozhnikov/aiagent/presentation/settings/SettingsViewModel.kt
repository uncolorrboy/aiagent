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
                settingsInteractor.observeMcpServerUrl(),
                settingsInteractor.observeMcpAuthToken(),
                settingsInteractor.observeMcpServerName(),
                settingsInteractor.observeMcpServerVersion(),
                settingsInteractor.observeMcpTools(),
            ) { values ->
                McpSettingsSnapshot(
                    enabled = values[0] as Boolean,
                    serverUrl = values[1] as String,
                    authToken = values[2] as String,
                    serverName = values[3] as String?,
                    serverVersion = values[4] as String?,
                    tools = values[5] as List<ru.sapozhnikov.aiagent.domain.model.McpToolDefinition>,
                )
            }.collect { snapshot ->
                _uiState.update { state ->
                    state.copy(
                        mcpEnabled = snapshot.enabled,
                        mcpServerUrl = snapshot.serverUrl,
                        mcpAuthToken = snapshot.authToken,
                        mcpServerName = snapshot.serverName,
                        mcpServerVersion = snapshot.serverVersion,
                        mcpTools = snapshot.tools,
                        mcpConnectionStatus = when {
                            state.mcpConnectionStatus == McpConnectionStatus.CONNECTING -> {
                                McpConnectionStatus.CONNECTING
                            }
                            snapshot.serverName != null -> McpConnectionStatus.CONNECTED
                            else -> McpConnectionStatus.DISCONNECTED
                        },
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
            if (!enabled) {
                _uiState.update {
                    it.copy(
                        mcpConnectionStatus = McpConnectionStatus.DISCONNECTED,
                        mcpConnectionError = null,
                    )
                }
            }
        }
    }

    fun onMcpServerUrlChanged(url: String) {
        _uiState.update { it.copy(mcpServerUrl = url, mcpConnectionError = null) }
    }

    fun onMcpAuthTokenChanged(token: String) {
        _uiState.update { it.copy(mcpAuthToken = token, mcpConnectionError = null) }
    }

    fun onMcpServerUrlFocusLost() {
        viewModelScope.launch {
            settingsInteractor.setMcpServerUrl(_uiState.value.mcpServerUrl)
        }
    }

    fun onMcpAuthTokenFocusLost() {
        viewModelScope.launch {
            settingsInteractor.setMcpAuthToken(_uiState.value.mcpAuthToken)
        }
    }

    fun onMcpConnectClicked() {
        val state = _uiState.value
        if (state.mcpServerUrl.isBlank()) {
            _uiState.update {
                it.copy(
                    mcpConnectionStatus = McpConnectionStatus.ERROR,
                    mcpConnectionError = "Укажите URL MCP-сервера",
                )
            }
            return
        }

        viewModelScope.launch {
            settingsInteractor.setMcpServerUrl(state.mcpServerUrl)
            settingsInteractor.setMcpAuthToken(state.mcpAuthToken)
            settingsInteractor.setMcpEnabled(true)

            _uiState.update {
                it.copy(
                    mcpConnectionStatus = McpConnectionStatus.CONNECTING,
                    mcpConnectionError = null,
                )
            }

            settingsInteractor.connectToMcpServer(
                serverUrl = state.mcpServerUrl,
                authToken = state.mcpAuthToken,
            ).onSuccess { connection ->
                _uiState.update {
                    it.copy(
                        mcpConnectionStatus = McpConnectionStatus.CONNECTED,
                        mcpServerName = connection.serverName,
                        mcpServerVersion = connection.serverVersion,
                        mcpTools = connection.tools,
                        mcpConnectionError = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        mcpConnectionStatus = McpConnectionStatus.ERROR,
                        mcpConnectionError = error.message ?: "Не удалось подключиться к MCP-серверу",
                    )
                }
            }
        }
    }

    fun onMcpDisconnectClicked() {
        viewModelScope.launch {
            settingsInteractor.disconnectFromMcpServer()
            _uiState.update {
                it.copy(
                    mcpConnectionStatus = McpConnectionStatus.DISCONNECTED,
                    mcpServerName = null,
                    mcpServerVersion = null,
                    mcpTools = emptyList(),
                    mcpConnectionError = null,
                )
            }
        }
    }

    private data class McpSettingsSnapshot(
        val enabled: Boolean,
        val serverUrl: String,
        val authToken: String,
        val serverName: String?,
        val serverVersion: String?,
        val tools: List<ru.sapozhnikov.aiagent.domain.model.McpToolDefinition>,
    )
}
