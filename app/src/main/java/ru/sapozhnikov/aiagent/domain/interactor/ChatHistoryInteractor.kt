package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.model.SavedUserFileMessage
import ru.sapozhnikov.aiagent.domain.repository.ChatHistoryRepository
import javax.inject.Inject

/** Use-case для управления историей чатов: сохранение сообщений, наблюдение за диалогами и метриками. */
internal class ChatHistoryInteractor @Inject constructor(
    private val repository: ChatHistoryRepository,
    private val conversationContextInteractor: ConversationContextInteractor,
) {

    /** Наблюдает за списком диалогов. */
    fun observeConversations(): Flow<List<Conversation>> = repository.observeConversations()

    /** Наблюдает за сообщениями указанного диалога. */
    fun observeMessages(conversationId: String): Flow<List<ChatHistoryMessage>> {
        return repository.observeMessages(conversationId)
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
        repository.saveMessage(conversationId, text, MessageRole.USER)
        if (isFirstMessage) {
            repository.updateConversationTitle(conversationId, text.toConversationTitle())
        }
    }

    /**
     * Сохраняет пользовательское файловое сообщение и возвращает его содержимое для API.
     * Для первого сообщения автоматически устанавливает заголовок диалога.
     */
    suspend fun saveUserFileMessage(conversationId: String, sourceUri: android.net.Uri): SavedUserFileMessage {
        repository.ensureConversationExists(conversationId)
        val isFirstMessage = repository.getMessages(conversationId).isEmpty()
        val savedFileMessage = repository.saveUserFileMessage(conversationId, sourceUri)
        if (isFirstMessage) {
            repository.updateConversationTitle(
                conversationId,
                savedFileMessage.fileName.toConversationTitle(),
            )
        }
        return savedFileMessage
    }

    /**
     * Сохраняет ответ ассистента и при необходимости запускает обновление резюме контекста.
     */
    suspend fun saveAiAgentMessage(conversationId: String, response: AiAgentMessage) {
        repository.saveAiAgentMessage(conversationId, response)
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
