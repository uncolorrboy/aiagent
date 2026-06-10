package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.repository.ChatHistoryRepository
import javax.inject.Inject

internal class ChatHistoryInteractor @Inject constructor(
    private val repository: ChatHistoryRepository,
) {

    fun observeConversations(): Flow<List<Conversation>> = repository.observeConversations()

    fun observeMessages(conversationId: String): Flow<List<ChatHistoryMessage>> {
        return repository.observeMessages(conversationId)
    }

    suspend fun getMessages(conversationId: String): List<ChatHistoryMessage> {
        return repository.getMessages(conversationId)
    }

    suspend fun saveUserMessage(conversationId: String, text: String) {
        repository.ensureConversationExists(conversationId)
        val isFirstMessage = repository.getMessages(conversationId).isEmpty()
        repository.saveMessage(conversationId, text, MessageRole.USER)
        if (isFirstMessage) {
            repository.updateConversationTitle(conversationId, text.toConversationTitle())
        }
    }

    suspend fun saveAiMessage(conversationId: String, text: String) {
        repository.saveMessage(conversationId, text, MessageRole.AI)
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
