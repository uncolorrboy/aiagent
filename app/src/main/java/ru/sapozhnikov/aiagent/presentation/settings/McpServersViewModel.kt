package ru.sapozhnikov.aiagent.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.app.McpOAuthCompletionEvent
import ru.sapozhnikov.aiagent.app.McpOAuthSessionManager
import ru.sapozhnikov.aiagent.domain.interactor.SettingsInteractor
import ru.sapozhnikov.aiagent.domain.model.McpOAuthAuthorizationStartedException
import ru.sapozhnikov.aiagent.domain.repository.McpToolRepository
import javax.inject.Inject

/** ViewModel экрана управления MCP-серверами. */
@HiltViewModel
internal class McpServersViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val mcpToolRepository: McpToolRepository,
    private val oauthSessionManager: McpOAuthSessionManager,
) : ViewModel() {

    private val runtimeState = MutableStateFlow(RuntimeState())
    private val _uiState = MutableStateFlow(McpServersUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsInteractor.observeMcpEnabled(),
                settingsInteractor.observeMcpServers(),
                runtimeState,
            ) { mcpEnabled, servers, runtime ->
                val activeConnections = mcpToolRepository.getCachedConnections().keys
                McpServersUiState(
                    mcpEnabled = mcpEnabled,
                    servers = servers.map { server ->
                        val status = when {
                            runtime.connectingServerIds.containsKey(server.id) -> McpConnectionStatus.CONNECTING
                            runtime.authorizingServerIds.contains(server.id) -> McpConnectionStatus.AUTHORIZING
                            activeConnections.contains(server.id) -> McpConnectionStatus.CONNECTED
                            runtime.errorByServerId.containsKey(server.id) -> McpConnectionStatus.ERROR
                            else -> McpConnectionStatus.DISCONNECTED
                        }
                        McpServerItemUiState(
                            server = server,
                            connectionStatus = status,
                            connectionError = runtime.errorByServerId[server.id],
                            toolsExpanded = runtime.expandedServerIds.contains(server.id),
                        )
                    },
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        viewModelScope.launch {
            oauthSessionManager.completionEvents.collect { event ->
                when (event) {
                    is McpOAuthCompletionEvent.Success -> {
                        runtimeState.update { runtime ->
                            runtime.copy(
                                authorizingServerIds = runtime.authorizingServerIds - event.serverId,
                                errorByServerId = runtime.errorByServerId - event.serverId,
                            )
                        }
                        connectServer(event.serverId, skipOAuthPrompt = true)
                    }
                    is McpOAuthCompletionEvent.Failure -> {
                        runtimeState.update { runtime ->
                            runtime.copy(
                                authorizingServerIds = runtime.authorizingServerIds - event.serverId,
                                errorByServerId = runtime.errorByServerId + (
                                    event.serverId to event.message
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }

    fun onMcpEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsInteractor.setMcpEnabled(enabled)
            if (!enabled) {
                runtimeState.update {
                    it.copy(
                        connectingServerIds = emptyMap(),
                        authorizingServerIds = emptySet(),
                        errorByServerId = emptyMap(),
                    )
                }
            }
        }
    }

    fun onServerEnabledChanged(serverId: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsInteractor.setMcpServerEnabled(serverId, enabled)
            if (!enabled) {
                runtimeState.update { runtime ->
                    runtime.copy(
                        connectingServerIds = runtime.connectingServerIds - serverId,
                        authorizingServerIds = runtime.authorizingServerIds - serverId,
                        errorByServerId = runtime.errorByServerId - serverId,
                    )
                }
            }
        }
    }

    fun onToolsExpandedToggled(serverId: String) {
        runtimeState.update { runtime ->
            val expanded = runtime.expandedServerIds.toMutableSet()
            if (expanded.contains(serverId)) {
                expanded.remove(serverId)
            } else {
                expanded.add(serverId)
            }
            runtime.copy(expandedServerIds = expanded)
        }
    }

    fun onConnectClicked(serverId: String) {
        connectServer(serverId)
    }

    fun onAuthorizeClicked(serverId: String) {
        viewModelScope.launch {
            val server = settingsInteractor.getMcpServers()
                .firstOrNull { it.id == serverId }
                ?: return@launch

            runtimeState.update { runtime ->
                runtime.copy(
                    authorizingServerIds = runtime.authorizingServerIds + serverId,
                    errorByServerId = runtime.errorByServerId - serverId,
                )
            }

            settingsInteractor.startMcpOAuthAuthorization(server)
                .onFailure { error ->
                    runtimeState.update { runtime ->
                        runtime.copy(
                            authorizingServerIds = runtime.authorizingServerIds - serverId,
                            errorByServerId = runtime.errorByServerId + (
                                serverId to (error.message ?: "Не удалось начать OAuth-авторизацию")
                                ),
                        )
                    }
                }
        }
    }

    fun onDisconnectClicked(serverId: String) {
        viewModelScope.launch {
            settingsInteractor.disconnectFromMcpServer(serverId)
            runtimeState.update { runtime ->
                runtime.copy(
                    connectingServerIds = runtime.connectingServerIds - serverId,
                    authorizingServerIds = runtime.authorizingServerIds - serverId,
                    errorByServerId = runtime.errorByServerId - serverId,
                )
            }
        }
    }

    fun onDeleteServer(serverId: String) {
        viewModelScope.launch {
            settingsInteractor.deleteMcpServer(serverId)
            runtimeState.update { runtime ->
                runtime.copy(
                    connectingServerIds = runtime.connectingServerIds - serverId,
                    authorizingServerIds = runtime.authorizingServerIds - serverId,
                    errorByServerId = runtime.errorByServerId - serverId,
                    expandedServerIds = runtime.expandedServerIds - serverId,
                )
            }
        }
    }

    private fun connectServer(serverId: String, skipOAuthPrompt: Boolean = false) {
        val server = _uiState.value.servers
            .firstOrNull { it.server.id == serverId }
            ?.server
            ?: return

        viewModelScope.launch {
            runtimeState.update { runtime ->
                runtime.copy(
                    connectingServerIds = runtime.connectingServerIds + (serverId to McpConnectionStatus.CONNECTING),
                    errorByServerId = runtime.errorByServerId - serverId,
                )
            }

            settingsInteractor.connectToMcpServer(server)
                .onSuccess {
                    runtimeState.update { runtime ->
                        runtime.copy(
                            connectingServerIds = runtime.connectingServerIds - serverId,
                            authorizingServerIds = runtime.authorizingServerIds - serverId,
                            errorByServerId = runtime.errorByServerId - serverId,
                        )
                    }
                }
                .onFailure { error ->
                    when {
                        error is McpOAuthAuthorizationStartedException -> {
                            runtimeState.update { runtime ->
                                runtime.copy(
                                    connectingServerIds = runtime.connectingServerIds - serverId,
                                    authorizingServerIds = runtime.authorizingServerIds + serverId,
                                )
                            }
                        }
                        else -> {
                            runtimeState.update { runtime ->
                                runtime.copy(
                                    connectingServerIds = runtime.connectingServerIds - serverId,
                                    errorByServerId = runtime.errorByServerId + (
                                        serverId to (error.message ?: "Не удалось подключиться к MCP-серверу")
                                        ),
                                )
                            }
                        }
                    }
                }
        }
    }

    private data class RuntimeState(
        val connectingServerIds: Map<String, McpConnectionStatus> = emptyMap(),
        val authorizingServerIds: Set<String> = emptySet(),
        val errorByServerId: Map<String, String> = emptyMap(),
        val expandedServerIds: Set<String> = emptySet(),
    )
}
