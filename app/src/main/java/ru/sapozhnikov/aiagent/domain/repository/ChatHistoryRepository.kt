package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.MessageRole

internal interface ChatHistoryRepository {

    fun observeConversations(): Flow<List<Conversation>>

    fun observeMessages(conversationId: String): Flow<List<ChatHistoryMessage>>

    suspend fun getMessages(conversationId: String): List<ChatHistoryMessage>

    suspend fun saveMessage(
        conversationId: String,
        text: String,
        role: MessageRole,
    )

    suspend fun ensureConversationExists(conversationId: String)

    suspend fun updateConversationTitle(conversationId: String, title: String)
}
