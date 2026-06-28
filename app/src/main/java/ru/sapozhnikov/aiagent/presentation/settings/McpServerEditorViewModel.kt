package ru.sapozhnikov.aiagent.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.SettingsInteractor
import ru.sapozhnikov.aiagent.domain.model.McpAuthType
import ru.sapozhnikov.aiagent.navigation.Screen
import javax.inject.Inject

/** ViewModel экрана создания и редактирования MCP-сервера. */
@HiltViewModel
internal class McpServerEditorViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val serverId: String? = savedStateHandle
        .get<String>(Screen.McpServerEditor.SERVER_ID_ARG)
        ?.takeUnless { it == Screen.McpServerEditor.NEW_SERVER_ID }

    private val _uiState = MutableStateFlow(
        McpServerEditorUiState(
            isEditMode = serverId != null,
            isLoading = serverId != null,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _saveCompleted = MutableStateFlow(false)
    val saveCompleted = _saveCompleted.asStateFlow()

    init {
        if (serverId != null) {
            viewModelScope.launch {
                val server = settingsInteractor.getMcpServers()
                    .firstOrNull { it.id == serverId }
                _uiState.update { state ->
                    if (server != null) {
                        state.copy(
                            url = server.url,
                            authType = server.authType,
                            authToken = server.authToken,
                            isLoading = false,
                        )
                    } else {
                        state.copy(isLoading = false)
                    }
                }
            }
        }
    }

    fun onUrlChanged(url: String) {
        _uiState.update { it.copy(url = url, errorMessage = null) }
    }

    fun onAuthTypeChanged(authType: McpAuthType) {
        _uiState.update {
            it.copy(
                authType = authType,
                authToken = if (authType == McpAuthType.BEARER) it.authToken else "",
                errorMessage = null,
            )
        }
    }

    fun onAuthTokenChanged(token: String) {
        _uiState.update { it.copy(authToken = token, errorMessage = null) }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val url = state.url.trim()
            val authToken = state.authToken.trim()

            if (serverId != null) {
                val existing = settingsInteractor.getMcpServers()
                    .firstOrNull { it.id == serverId }
                if (existing == null) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Сервер не найден",
                        )
                    }
                    return@launch
                }
                settingsInteractor.updateMcpServer(
                    existing.copy(
                        url = url,
                        authType = state.authType,
                        authToken = authToken,
                        oauthTokens = if (state.authType == McpAuthType.OAUTH) existing.oauthTokens else null,
                        oauthClientInfo = if (state.authType == McpAuthType.OAUTH) existing.oauthClientInfo else null,
                        oauthDiscovery = if (state.authType == McpAuthType.OAUTH) existing.oauthDiscovery else null,
                        serverName = null,
                        serverVersion = null,
                        tools = emptyList(),
                    ),
                )
                settingsInteractor.disconnectFromMcpServer(serverId)
            } else {
                settingsInteractor.addMcpServer(
                    url = url,
                    authType = state.authType,
                    authToken = authToken,
                )
            }

            _uiState.update { it.copy(isSaving = false) }
            _saveCompleted.value = true
        }
    }
}
