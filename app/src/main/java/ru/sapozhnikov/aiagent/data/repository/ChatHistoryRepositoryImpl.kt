package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sapozhnikov.aiagent.data.local.dao.ConversationDao
import ru.sapozhnikov.aiagent.data.local.dao.MessageDao
import ru.sapozhnikov.aiagent.data.local.entity.ConversationEntity
import ru.sapozhnikov.aiagent.data.local.entity.MessageEntity
import ru.sapozhnikov.aiagent.data.local.mapper.toDomain
import ru.sapozhnikov.aiagent.data.local.mapper.toEntityRole
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.repository.ChatHistoryRepository
import javax.inject.Inject

internal class ChatHistoryRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
) : ChatHistoryRepository {

    override fun observeConversations(): Flow<List<Conversation>> {
        return conversationDao.observeConversations().map { conversations ->
            conversations.map { it.toDomain() }
        }
    }

    override fun observeMessages(conversationId: String): Flow<List<ChatHistoryMessage>> {
        return messageDao.observeMessages(conversationId).map { messages ->
            messages.map { it.toDomain() }
        }
    }

    override suspend fun getMessages(conversationId: String): List<ChatHistoryMessage> {
        return messageDao.getMessages(conversationId).map { it.toDomain() }
    }

    override suspend fun saveMessage(
        conversationId: String,
        text: String,
        role: MessageRole,
    ) {
        val now = System.currentTimeMillis()
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                text = text,
                role = role.toEntityRole(),
                timestamp = now,
            ),
        )
        conversationDao.getById(conversationId)?.let { conversation ->
            conversationDao.update(conversation.copy(updatedAt = now))
        }
    }

    override suspend fun ensureConversationExists(conversationId: String) {
        if (conversationDao.getById(conversationId) != null) return
        val now = System.currentTimeMillis()
        conversationDao.insert(
            ConversationEntity(
                id = conversationId,
                title = DEFAULT_TITLE,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun updateConversationTitle(conversationId: String, title: String) {
        conversationDao.getById(conversationId)?.let { conversation ->
            conversationDao.update(conversation.copy(title = title, updatedAt = System.currentTimeMillis()))
        }
    }

    private companion object {
        const val DEFAULT_TITLE = "Новый чат"
    }
}
