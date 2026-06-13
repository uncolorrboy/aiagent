package ru.sapozhnikov.aiagent.presentation.chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.AiAgentInteractor
import ru.sapozhnikov.aiagent.domain.interactor.ChatHistoryInteractor
import ru.sapozhnikov.aiagent.domain.interactor.ConversationBranchInteractor
import ru.sapozhnikov.aiagent.domain.interactor.SettingsInteractor
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import javax.inject.Inject

/**
 * ViewModel экрана чата: отправка сообщений, файлов и наблюдение за историей.
 *
 * @param aiAgentInteractor use-case для отправки сообщений LLM
 * @param chatHistoryInteractor use-case для работы с историей чатов
 * @param conversationBranchInteractor use-case для управления ветками
 * @param settingsInteractor use-case для чтения настроек
 * @param savedStateHandle аргументы навигации (conversationId)
 */
@HiltViewModel
internal class ChatViewModel @Inject constructor(
    private val aiAgentInteractor: AiAgentInteractor,
    private val chatHistoryInteractor: ChatHistoryInteractor,
    private val conversationBranchInteractor: ConversationBranchInteractor,
    private val settingsInteractor: SettingsInteractor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    private val _uiState = MutableStateFlow(ChatScreenUiState())
    /** Состояние UI экрана чата. */
    val uiState = _uiState.asStateFlow()

    private val _errorEvents = Channel<String>(Channel.BUFFERED)
    /** Одноразовые события ошибок для отображения пользователю. */
    val errorEvents = _errorEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            chatHistoryInteractor.observeMessages(conversationId).collect { messages ->
                _uiState.update { state ->
                    state.copy(items = messages.reversed().map { it.toUiModel() })
                }
            }
        }
        viewModelScope.launch {
            chatHistoryInteractor.observeTotalTokenCount(conversationId).collect { totalTokens ->
                _uiState.update { state ->
                    state.copy(totalTokenCount = totalTokens)
                }
            }
        }
        viewModelScope.launch {
            chatHistoryInteractor.observeChatCost(conversationId).collect { chatPrice ->
                _uiState.update { state ->
                    state.copy(chatPrice = chatPrice)
                }
            }
        }
        viewModelScope.launch {
            combine(
                settingsInteractor.observeContextManagementStrategy(),
                conversationBranchInteractor.observeBranches(conversationId),
                chatHistoryInteractor.observeMessages(conversationId),
            ) { strategy, branches, messages ->
                Triple(strategy, branches, messages)
            }.collect { (strategy, branches, messages) ->
                _uiState.update { state ->
                    state.copy(
                        contextStrategy = strategy,
                        branches = branches.map { branch ->
                            ChatBranchUi(
                                id = branch.id,
                                name = branch.name,
                                isActive = branch.isActive,
                            )
                        },
                        canCreateCheckpoint = strategy == ContextManagementStrategy.BRANCHING &&
                            branches.isEmpty() &&
                            messages.isNotEmpty(),
                    )
                }
            }
        }
    }

    /**
     * Обрабатывает отправку текстового сообщения пользователем.
     *
     * @param message текст сообщения
     */
    fun onMessageSent(message: String) {
        if (message.isBlank() || _uiState.value.isLoading) return

        val trimmedMessage = message.trim()

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    sendButtonState = SendButtonState.IN_PROCESS,
                )
            }

            val context = chatHistoryInteractor.getContextForApi(conversationId)
            chatHistoryInteractor.saveUserMessage(conversationId, trimmedMessage)

            aiAgentInteractor.sendMessage(context, trimmedMessage)
                .onSuccess { response ->
                    chatHistoryInteractor.saveAiAgentMessage(conversationId, response)
                }
                .onFailure { error ->
                    _errorEvents.send(error.toUserMessage())
                }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    sendButtonState = SendButtonState.DEFAULT,
                )
            }
        }
    }

    /**
     * Обрабатывает выбор текстового файла для отправки в чат.
     *
     * @param uri URI выбранного файла
     */
    fun onTextFileSelected(uri: Uri) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    sendButtonState = SendButtonState.IN_PROCESS,
                )
            }

            runCatching {
                val context = chatHistoryInteractor.getContextForApi(conversationId)
                val savedFileMessage = chatHistoryInteractor.saveUserFileMessage(conversationId, uri)

                aiAgentInteractor.sendMessage(context, savedFileMessage.content)
                    .onSuccess { response ->
                        chatHistoryInteractor.saveAiAgentMessage(conversationId, response)
                    }
                    .onFailure { error ->
                        _errorEvents.send(error.toUserMessage())
                    }
            }.onFailure { error ->
                _errorEvents.send(error.toUserMessage())
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    sendButtonState = SendButtonState.DEFAULT,
                )
            }
        }
    }

    /** Создаёт checkpoint и две ветки от текущего места диалога. */
    fun onCreateCheckpoint() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            conversationBranchInteractor.createCheckpoint(conversationId)
                .onFailure { error ->
                    _errorEvents.send(
                        error.localizedMessage ?: "Не удалось создать checkpoint",
                    )
                }
        }
    }

    /** Переключает активную ветку диалога. */
    fun onBranchSelected(branchId: String) {
        viewModelScope.launch {
            conversationBranchInteractor.switchBranch(conversationId, branchId)
        }
    }

    private fun Throwable.toUserMessage(): String {
        val details = localizedMessage?.takeIf { it.isNotBlank() }
        return if (details != null) {
            "Не удалось отправить сообщение: $details"
        } else {
            "Не удалось отправить сообщение. Попробуйте ещё раз"
        }
    }
}
