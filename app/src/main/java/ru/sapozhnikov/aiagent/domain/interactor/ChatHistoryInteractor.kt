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

internal class ChatHistoryInteractor @Inject constructor(
    private val repository: ChatHistoryRepository,
    private val conversationContextInteractor: ConversationContextInteractor,
) {

    fun observeConversations(): Flow<List<Conversation>> = repository.observeConversations()

    fun observeMessages(conversationId: String): Flow<List<ChatHistoryMessage>> {
        return repository.observeMessages(conversationId)
    }

    suspend fun getContextForApi(conversationId: String): ApiConversationContext {
        return conversationContextInteractor.getContextForApi(conversationId)
    }

    suspend fun saveUserMessage(conversationId: String, text: String) {
        repository.ensureConversationExists(conversationId)
        val isFirstMessage = repository.getMessages(conversationId).isEmpty()
        repository.saveMessage(conversationId, text, MessageRole.USER)
        if (isFirstMessage) {
            repository.updateConversationTitle(conversationId, text.toConversationTitle())
        }
    }

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

    suspend fun saveAiAgentMessage(conversationId: String, response: AiAgentMessage) {
        repository.saveAiAgentMessage(conversationId, response)
        conversationContextInteractor.updateSummaryIfNeeded(conversationId)
    }

    fun observeTotalTokenCount(conversationId: String) = repository.observeTotalTokenCount(conversationId)

    fun observeChatCost(conversationId: String) = repository.observeChatCost(conversationId)

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
