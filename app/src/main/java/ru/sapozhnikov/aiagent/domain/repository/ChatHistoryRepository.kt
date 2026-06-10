package ru.sapozhnikov.aiagent.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.model.SavedUserFileMessage

internal interface ChatHistoryRepository {

    fun observeConversations(): Flow<List<Conversation>>

    fun observeMessages(conversationId: String): Flow<List<ChatHistoryMessage>>

    fun observeTotalTokenCount(conversationId: String): Flow<Int>

    fun observeChatCost(conversationId: String): Flow<Double>

    suspend fun getMessages(conversationId: String): List<ChatHistoryMessage>

    suspend fun getMessagesForApi(conversationId: String): List<ChatHistoryMessage>

    suspend fun saveMessage(conversationId: String, text: String, role: MessageRole)

    suspend fun saveUserFileMessage(conversationId: String, sourceUri: Uri): SavedUserFileMessage

    suspend fun saveAiAgentMessage(conversationId: String, aiAgentMessage: AiAgentMessage)

    suspend fun ensureConversationExists(conversationId: String)

    suspend fun updateConversationTitle(conversationId: String, title: String)
}
