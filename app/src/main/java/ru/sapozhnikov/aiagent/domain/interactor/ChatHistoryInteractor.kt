package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.model.SavedUserFileMessage
import ru.sapozhnikov.aiagent.domain.repository.ChatHistoryRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationBranchRepository
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject

/** Use-case для управления историей чатов: сохранение сообщений, наблюдение за диалогами и метриками. */
@OptIn(ExperimentalCoroutinesApi::class)
internal class ChatHistoryInteractor @Inject constructor(
    private val repository: ChatHistoryRepository,
    private val conversationContextInteractor: ConversationContextInteractor,
    private val conversationBranchRepository: ConversationBranchRepository,
    private val settingsRepository: SettingsRepository,
) {

    /** Наблюдает за списком диалогов. */
    fun observeConversations(): Flow<List<Conversation>> = repository.observeConversations()

    /** Наблюдает за сообщениями указанного диалога с учётом стратегии контекста. */
    fun observeMessages(conversationId: String): Flow<List<ChatHistoryMessage>> {
        return observeMessagesInternal(conversationId)
    }

    /** Возвращает контекст диалога, подготовленный для отправки в LLM API. */
    suspend fun getContextForApi(conversationId: String): ApiConversationContext {
        return conversationContextInteractor.getContextForApi(conversationId)
    }

    /**
     * Сохраняет пользовательское текстовое сообщение.
     * Для первого сообщения автоматически устанавливает заголовок диалога.
     */
    suspend fun saveUserMessage(conversationId: String, text: String) {
        repository.ensureConversationExists(conversationId)
        val isFirstMessage = repository.getMessages(conversationId).isEmpty()
        val branchId = resolveActiveBranchId(conversationId)
        repository.saveMessage(conversationId, text, MessageRole.USER, branchId)
        if (isFirstMessage) {
            repository.updateConversationTitle(conversationId, text.toConversationTitle())
        }
        conversationContextInteractor.updateFactsIfNeeded(conversationId, text)
    }

    /**
     * Сохраняет пользовательское файловое сообщение и возвращает его содержимое для API.
     * Для первого сообщения автоматически устанавливает заголовок диалога.
     */
    suspend fun saveUserFileMessage(conversationId: String, sourceUri: android.net.Uri): SavedUserFileMessage {
        repository.ensureConversationExists(conversationId)
        val isFirstMessage = repository.getMessages(conversationId).isEmpty()
        val branchId = resolveActiveBranchId(conversationId)
        val savedFileMessage = repository.saveUserFileMessage(conversationId, sourceUri, branchId)
        if (isFirstMessage) {
            repository.updateConversationTitle(
                conversationId,
                savedFileMessage.fileName.toConversationTitle(),
            )
        }
        conversationContextInteractor.updateFactsIfNeeded(conversationId, savedFileMessage.content)
        return savedFileMessage
    }

    /** Сохраняет ответ ассистента. */
    suspend fun saveAiAgentMessage(conversationId: String, response: AiAgentMessage) {
        val branchId = resolveActiveBranchId(conversationId)
        repository.saveAiAgentMessage(conversationId, response, branchId)
        conversationContextInteractor.updateSummaryIfNeeded(conversationId)
    }

    /** Наблюдает за суммарным числом токенов в диалоге. */
    fun observeTotalTokenCount(conversationId: String) = repository.observeTotalTokenCount(conversationId)

    /** Наблюдает за расчётной стоимостью диалога. */
    fun observeChatCost(conversationId: String) = repository.observeChatCost(conversationId)

    /** Удаляет диалог и все связанные данные. */
    suspend fun deleteConversation(conversationId: String) {
        repository.deleteConversation(conversationId)
    }

    private fun observeMessagesInternal(conversationId: String): Flow<List<ChatHistoryMessage>> {
        return settingsRepository.observeContextManagementStrategy().flatMapLatest { strategy ->
            if (strategy == ContextManagementStrategy.BRANCHING) {
                conversationBranchRepository.observeBranches(conversationId).flatMapLatest { branches ->
                    val activeBranch = branches.find { it.isActive }
                    if (activeBranch != null) {
                        repository.observeMessagesForBranch(conversationId, activeBranch.id)
                    } else {
                        repository.observeMessages(conversationId)
                    }
                }
            } else {
                repository.observeMessages(conversationId)
            }
        }
    }

    private suspend fun resolveActiveBranchId(conversationId: String): String? {
        if (settingsRepository.getContextManagementStrategy() != ContextManagementStrategy.BRANCHING) {
            return null
        }
        return conversationBranchRepository.getActiveBranch(conversationId)?.id
    }

    private fun String.toConversationTitle(): String {
        val trimmed = trim()
        return if (trimmed.length <= MAX_TITLE_LENGTH) {
            trimmed
        } else {
            trimmed.take(MAX_TITLE_LENGTH) + "…"
        }
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 50
    }
}
