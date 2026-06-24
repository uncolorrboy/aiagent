package ru.sapozhnikov.aiagent.presentation.chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.AgentOrchestratorInteractor
import ru.sapozhnikov.aiagent.domain.interactor.ChatHistoryInteractor
import ru.sapozhnikov.aiagent.domain.interactor.ConversationBranchInteractor
import ru.sapozhnikov.aiagent.domain.interactor.InvariantInteractor
import ru.sapozhnikov.aiagent.domain.interactor.MemoryInteractor
import ru.sapozhnikov.aiagent.domain.interactor.SettingsInteractor
import ru.sapozhnikov.aiagent.domain.interactor.TaskInteractor
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.ConversationMemorySelection
import ru.sapozhnikov.aiagent.domain.model.ConversationMode
import ru.sapozhnikov.aiagent.domain.model.TaskStage
import ru.sapozhnikov.aiagent.domain.model.TaskStagePrompts
import ru.sapozhnikov.aiagent.domain.model.TaskStageTransitions
import ru.sapozhnikov.aiagent.domain.model.advanceActionLabel
import ru.sapozhnikov.aiagent.domain.model.revertActionLabel
import javax.inject.Inject

/**
 * ViewModel экрана чата: отправка сообщений, файлов и наблюдение за историей.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class ChatViewModel @Inject constructor(
    private val agentOrchestratorInteractor: AgentOrchestratorInteractor,
    private val chatHistoryInteractor: ChatHistoryInteractor,
    private val conversationBranchInteractor: ConversationBranchInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val memoryInteractor: MemoryInteractor,
    private val invariantInteractor: InvariantInteractor,
    private val taskInteractor: TaskInteractor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    private val _uiState = MutableStateFlow(ChatScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val isTaskModeFlow = MutableStateFlow(false)

    private val _errorEvents = Channel<String>(Channel.BUFFERED)
    val errorEvents = _errorEvents.receiveAsFlow()

    private var pendingTransitionJob: Job? = null

    init {
        viewModelScope.launch {
            val mode = chatHistoryInteractor.getConversationMode(conversationId)
            val isTaskMode = mode == ConversationMode.TASK
            isTaskModeFlow.value = isTaskMode
            _uiState.update { state ->
                state.copy(mode = mode, isTaskMode = isTaskMode)
            }
        }

        viewModelScope.launch {
            combine(
                isTaskModeFlow,
                taskInteractor.observeTaskState(conversationId),
            ) { isTaskMode, taskState ->
                isTaskMode to taskState
            }.flatMapLatest { (isTaskMode, taskState) ->
                when {
                    isTaskMode && taskState != null ->
                        chatHistoryInteractor.observeMessagesForTaskStage(
                            conversationId,
                            taskState.viewingStage,
                        )
                    !isTaskMode ->
                        chatHistoryInteractor.observeMessages(conversationId)
                    else -> flowOf(emptyList())
                }
            }.collect { messages ->
                _uiState.update { state ->
                    state.copy(
                        items = messages.reversed().map { it.toUiModel() },
                    )
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
                        canCreateCheckpoint = !state.isTaskMode &&
                            strategy == ContextManagementStrategy.BRANCHING &&
                            branches.isEmpty() &&
                            messages.isNotEmpty(),
                    )
                }
            }
        }
        viewModelScope.launch {
            combine(
                memoryInteractor.observeWorkingMemoryInstances(),
                memoryInteractor.observeProfileMemoryInstances(),
                memoryInteractor.observeConversationMemorySelection(conversationId),
            ) { working, profile, selection ->
                Triple(working, profile, selection)
            }.collect { (working, profile, selection) ->
                _uiState.update { state ->
                    state.copy(
                        workingMemoryInstances = working,
                        profileMemoryInstances = profile,
                        selectedWorkingMemoryId = selection?.workingMemoryId,
                        selectedProfileMemoryId = selection?.profileMemoryId,
                        isMemorySelectionLocked = selection != null,
                    )
                }
            }
        }
        viewModelScope.launch {
            combine(
                invariantInteractor.observeAllInvariants(),
                invariantInteractor.observeConversationInvariantIds(conversationId),
            ) { allInvariants, selectedIds ->
                allInvariants to selectedIds.toSet()
            }.collect { (allInvariants, selectedIds) ->
                _uiState.update { state ->
                    state.copy(
                        availableInvariants = allInvariants,
                        selectedInvariantIds = selectedIds,
                    )
                }
            }
        }
        viewModelScope.launch {
            taskInteractor.observeTaskState(conversationId).collect { taskState ->
                _uiState.update { state ->
                    val hasPendingTransition = state.pendingTaskTransition != null
                    val controls = computeTaskStageControls(
                        isTaskMode = state.isTaskMode,
                        activeStage = taskState?.activeStage,
                        viewingStage = taskState?.viewingStage,
                        hasPendingTransition = hasPendingTransition,
                    )
                    state.copy(
                        activeTaskStage = taskState?.activeStage,
                        viewingTaskStage = taskState?.viewingStage,
                        isInputEnabled = controls.isInputEnabled,
                        canAdvanceTaskStage = controls.canAdvance,
                        advanceTaskStageLabel = controls.advanceLabel,
                        canRevertTaskStage = controls.canRevert,
                        revertTaskStageLabel = controls.revertLabel,
                    )
                }
            }
        }
    }

    /** Инициализирует экран как новую задачу (вызывается из навигации). */
    fun initializeAsTaskMode() {
        viewModelScope.launch {
            chatHistoryInteractor.initializeTaskMode(conversationId)
            isTaskModeFlow.value = true
            _uiState.update { state ->
                state.copy(mode = ConversationMode.TASK, isTaskMode = true)
            }
        }
    }

    fun onTaskStageSelected(stage: TaskStage) {
        viewModelScope.launch {
            taskInteractor.switchViewingStage(conversationId, stage)
        }
    }

    /** Переход на следующий этап по инициативе пользователя. */
    fun onAdvanceTaskStage() {
        if (_uiState.value.isLoading || !_uiState.value.canAdvanceTaskStage) return

        viewModelScope.launch {
            cancelPendingTransition()
            val currentStage = taskInteractor.getTaskState(conversationId)?.activeStage ?: return@launch
            val newStage = taskInteractor.advanceToNextStage(conversationId) ?: return@launch
            performTaskStageTransition(newStage, currentStage)
        }
    }

    /** Возврат на предыдущий этап по инициативе пользователя. */
    fun onRevertTaskStage() {
        if (_uiState.value.isLoading || !_uiState.value.canRevertTaskStage) return

        viewModelScope.launch {
            cancelPendingTransition()
            val currentStage = taskInteractor.getTaskState(conversationId)?.activeStage ?: return@launch
            val newStage = taskInteractor.revertToPreviousStage(conversationId) ?: return@launch
            performTaskStageTransition(newStage, currentStage)
        }
    }

    fun onMemorySettingsClicked() {
        _uiState.update { it.copy(isMemorySheetVisible = true) }
    }

    fun onDismissMemorySheet() {
        _uiState.update { it.copy(isMemorySheetVisible = false) }
    }

    fun onInvariantsSettingsClicked() {
        _uiState.update { it.copy(isInvariantsSheetVisible = true) }
    }

    fun onDismissInvariantsSheet() {
        _uiState.update { it.copy(isInvariantsSheetVisible = false) }
    }

    fun onSaveInvariantSelection(invariantIds: Set<String>) {
        viewModelScope.launch {
            invariantInteractor.saveConversationInvariantIds(conversationId, invariantIds.toList())
            _uiState.update { it.copy(isInvariantsSheetVisible = false) }
        }
    }

    fun onSaveMemorySelection(workingMemoryId: String?, profileMemoryId: String?) {
        if (_uiState.value.isMemorySelectionLocked) return

        viewModelScope.launch {
            memoryInteractor.saveConversationMemorySelection(
                ConversationMemorySelection(
                    conversationId = conversationId,
                    workingMemoryId = workingMemoryId,
                    profileMemoryId = profileMemoryId,
                ),
            )
            _uiState.update { it.copy(isMemorySheetVisible = false) }
        }
    }

    fun onMessageSent(message: String) {
        if (message.isBlank() || _uiState.value.isLoading || !_uiState.value.isInputEnabled) return

        viewModelScope.launch {
            cancelPendingTransition()
            sendMessageWithAiResponse(message.trim())
        }
    }

    fun onTextFileSelected(uri: Uri) {
        if (_uiState.value.isLoading || !_uiState.value.isInputEnabled) return
        val taskStage = _uiState.value.activeTaskStage.takeIf { _uiState.value.isTaskMode }

        viewModelScope.launch {
            cancelPendingTransition()
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    sendButtonState = SendButtonState.IN_PROCESS,
                )
            }

            runCatching {
                val savedFileMessage = chatHistoryInteractor.saveUserFileMessage(
                    conversationId,
                    uri,
                    taskStage,
                )
                sendMessageWithAiResponse(savedFileMessage.content, taskStage)
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

    fun onCreateCheckpoint() {
        if (_uiState.value.isLoading || _uiState.value.isTaskMode) return

        viewModelScope.launch {
            conversationBranchInteractor.createCheckpoint(conversationId)
                .onFailure { error ->
                    _errorEvents.send(
                        error.localizedMessage ?: "Не удалось создать checkpoint",
                    )
                }
        }
    }

    fun onBranchSelected(branchId: String) {
        viewModelScope.launch {
            conversationBranchInteractor.switchBranch(conversationId, branchId)
        }
    }

    private suspend fun sendMessageWithAiResponse(
        message: String,
        taskStage: TaskStage? = _uiState.value.activeTaskStage.takeIf { _uiState.value.isTaskMode },
    ) {
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                sendButtonState = SendButtonState.IN_PROCESS,
            )
        }

        val context = chatHistoryInteractor.getContextForApi(conversationId)
        chatHistoryInteractor.saveUserMessage(conversationId, message, taskStage)

        val aiResult = agentOrchestratorInteractor.sendMessage(context, message)
        if (aiResult.isSuccess) {
            val response = aiResult.getOrThrow()
            chatHistoryInteractor.saveAiAgentMessage(
                conversationId,
                response,
                taskStage,
            )
            scheduleTransitionFromAssistantResponse(response.text, taskStage)
        } else {
            aiResult.exceptionOrNull()?.let { _errorEvents.send(it.toUserMessage()) }
        }

        _uiState.update { state ->
            state.copy(
                isLoading = false,
                sendButtonState = SendButtonState.DEFAULT,
            )
        }
    }

    private suspend fun performTaskStageTransition(
        stage: TaskStage,
        fromStage: TaskStage,
    ) {
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                sendButtonState = SendButtonState.IN_PROCESS,
            )
        }

        sendTaskStageContinuation(stage, fromStage)

        _uiState.update { state ->
            state.copy(
                isLoading = false,
                sendButtonState = SendButtonState.DEFAULT,
            )
        }
    }

    private fun scheduleTransitionFromAssistantResponse(
        responseText: String,
        currentStage: TaskStage?,
    ) {
        if (!_uiState.value.isTaskMode || currentStage == null) return

        val targetStage = TaskStageTransitions.parseTransitionMarker(responseText) ?: return
        if (!TaskStageTransitions.isValidTransition(currentStage, targetStage)) return

        pendingTransitionJob?.cancel()
        pendingTransitionJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / TRANSITION_DELAY_MS).coerceIn(0f, 1f)
                _uiState.update { state ->
                    state.copy(
                        pendingTaskTransition = PendingTaskStageTransition(
                            targetStage = targetStage,
                            fromStage = currentStage,
                            progress = progress,
                        ),
                        isInputEnabled = false,
                        canAdvanceTaskStage = false,
                        canRevertTaskStage = false,
                    )
                }
                if (elapsed >= TRANSITION_DELAY_MS) break
                delay(PROGRESS_UPDATE_INTERVAL_MS)
            }

            _uiState.update { it.copy(pendingTaskTransition = null) }
            val transitionedStage = taskInteractor.transitionToStage(conversationId, targetStage)
            if (transitionedStage != null) {
                performTaskStageTransition(transitionedStage, currentStage)
            }
        }
    }

    private fun cancelPendingTransition() {
        pendingTransitionJob?.cancel()
        pendingTransitionJob = null
        _uiState.update { state ->
            if (state.pendingTaskTransition == null) return@update state
            val controls = computeTaskStageControls(
                isTaskMode = state.isTaskMode,
                activeStage = state.activeTaskStage,
                viewingStage = state.viewingTaskStage,
                hasPendingTransition = false,
            )
            state.copy(
                pendingTaskTransition = null,
                isInputEnabled = controls.isInputEnabled,
                canAdvanceTaskStage = controls.canAdvance,
                advanceTaskStageLabel = controls.advanceLabel,
                canRevertTaskStage = controls.canRevert,
                revertTaskStageLabel = controls.revertLabel,
            )
        }
    }

    private fun computeTaskStageControls(
        isTaskMode: Boolean,
        activeStage: TaskStage?,
        viewingStage: TaskStage?,
        hasPendingTransition: Boolean,
    ): TaskStageControls {
        if (!isTaskMode || activeStage == null || hasPendingTransition) {
            return TaskStageControls(
                isInputEnabled = !isTaskMode,
                canAdvance = false,
                advanceLabel = null,
                canRevert = false,
                revertLabel = null,
            )
        }

        val isViewingActiveStage = viewingStage == activeStage
        val canAdvance = isViewingActiveStage &&
            activeStage != TaskStage.DONE &&
            activeStage.next() != null
        val canRevert = isViewingActiveStage &&
            TaskStageTransitions.defaultPreviousStage(activeStage) != null

        return TaskStageControls(
            isInputEnabled = isViewingActiveStage && activeStage != TaskStage.DONE,
            canAdvance = canAdvance,
            advanceLabel = activeStage.advanceActionLabel(),
            canRevert = canRevert,
            revertLabel = activeStage.revertActionLabel(),
        )
    }

    private suspend fun sendTaskStageContinuation(
        stage: TaskStage,
        fromStage: TaskStage,
    ) {
        chatHistoryInteractor.saveTaskStageContinuationMessage(
            conversationId,
            stage,
            fromStage,
        )

        val context = chatHistoryInteractor.getContextForApi(conversationId)
        val continuationMessage = TaskStagePrompts.continuationMessageFor(stage, fromStage) ?: return

        val aiResult = agentOrchestratorInteractor.sendMessage(context, continuationMessage)
        if (aiResult.isSuccess) {
            chatHistoryInteractor.saveAiAgentMessage(
                conversationId,
                aiResult.getOrThrow(),
                stage,
            )
        } else {
            aiResult.exceptionOrNull()?.let { _errorEvents.send(it.toUserMessage()) }
        }
    }

    private data class TaskStageControls(
        val isInputEnabled: Boolean,
        val canAdvance: Boolean,
        val advanceLabel: String?,
        val canRevert: Boolean,
        val revertLabel: String?,
    )

    private fun Throwable.toUserMessage(): String {
        val details = localizedMessage?.takeIf { it.isNotBlank() }
        return if (details != null) {
            "Не удалось отправить сообщение: $details"
        } else {
            "Не удалось отправить сообщение. Попробуйте ещё раз"
        }
    }

    private companion object {
        const val TRANSITION_DELAY_MS = 3_000L
        const val PROGRESS_UPDATE_INTERVAL_MS = 50L
    }
}
