package ru.sapozhnikov.aiagent.data.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sapozhnikov.aiagent.data.local.PersistedTextAttachmentBuilder
import ru.sapozhnikov.aiagent.data.local.dao.ConversationDao
import ru.sapozhnikov.aiagent.data.local.dao.MessageDao
import ru.sapozhnikov.aiagent.data.local.entity.ConversationEntity
import ru.sapozhnikov.aiagent.data.local.entity.MessageEntity
import ru.sapozhnikov.aiagent.data.local.mapper.toDomain
import ru.sapozhnikov.aiagent.data.local.mapper.toEntityKind
import ru.sapozhnikov.aiagent.data.local.mapper.toEntityRole
import ru.sapozhnikov.aiagent.data.local.mapper.toEntityStage
import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.ConversationMode
import ru.sapozhnikov.aiagent.domain.model.MessageKind
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.model.SavedUserFileMessage
import ru.sapozhnikov.aiagent.domain.model.TaskStage
import ru.sapozhnikov.aiagent.domain.model.calculateChatCost
import ru.sapozhnikov.aiagent.domain.repository.ChatHistoryRepository
import javax.inject.Inject

/** Реализация [ChatHistoryRepository] на базе Room и локального хранилища файлов. */
internal class ChatHistoryRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val persistedTextAttachmentBuilder: PersistedTextAttachmentBuilder,
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

    override fun observeMessagesForTaskStage(
        conversationId: String,
        taskStage: TaskStage,
    ): Flow<List<ChatHistoryMessage>> {
        return messageDao.observeMessagesForTaskStage(conversationId, taskStage.toEntityStage()).map { messages ->
            messages.map { it.toDomain() }
        }
    }

    override fun observeMessagesForBranch(
        conversationId: String,
        activeBranchId: String,
    ): Flow<List<ChatHistoryMessage>> {
        return messageDao.observeMessagesForBranch(conversationId, activeBranchId).map { messages ->
            messages.map { it.toDomain() }
        }
    }

    override suspend fun getMessages(conversationId: String): List<ChatHistoryMessage> {
        return messageDao.getMessages(conversationId).map { it.toDomain() }
    }

    override suspend fun getMessagesForApi(conversationId: String): List<ChatHistoryMessage> {
        return messageDao.getMessages(conversationId).map { entity ->
            entity.toDomain().resolveTextForApi()
        }
    }

    override suspend fun getMessagesForApi(
        conversationId: String,
        activeBranchId: String,
    ): List<ChatHistoryMessage> {
        return messageDao.getMessagesForBranch(conversationId, activeBranchId).map { entity ->
            entity.toDomain().resolveTextForApi()
        }
    }

    override suspend fun getMessagesForApiByTaskStage(
        conversationId: String,
        taskStage: TaskStage,
    ): List<ChatHistoryMessage> {
        return messageDao.getMessagesForTaskStage(conversationId, taskStage.toEntityStage()).map { entity ->
            entity.toDomain().resolveTextForApi()
        }
    }

    override suspend fun getConversationMode(conversationId: String): ConversationMode {
        return conversationDao.getById(conversationId)?.toDomain()?.mode ?: ConversationMode.CHAT
    }

    override suspend fun saveMessage(
        conversationId: String,
        text: String,
        role: MessageRole,
        branchId: String?,
        taskStage: TaskStage?,
    ) {
        val now = System.currentTimeMillis()
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                text = text,
                role = role.toEntityRole(),
                timestamp = now,
                branchId = branchId,
                taskStage = taskStage?.toEntityStage(),
            ),
        )
        touchConversation(conversationId, now)
    }

    override suspend fun saveUserFileMessage(
        conversationId: String,
        sourceUri: Uri,
        branchId: String?,
        taskStage: TaskStage?,
    ): SavedUserFileMessage {
        val attachment = persistedTextAttachmentBuilder.build(conversationId, sourceUri)
        val now = System.currentTimeMillis()
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                text = "",
                role = MessageRole.USER.toEntityRole(),
                timestamp = now,
                kind = MessageKind.FILE.toEntityKind(),
                attachmentUri = attachment.storedUri,
                attachmentFileName = attachment.fileName,
                branchId = branchId,
                taskStage = taskStage?.toEntityStage(),
            ),
        )
        touchConversation(conversationId, now)
        return SavedUserFileMessage(
            content = attachment.content,
            fileName = attachment.fileName,
        )
    }

    override suspend fun saveAiAgentMessage(
        conversationId: String,
        aiAgentMessage: AiAgentMessage,
        branchId: String?,
        taskStage: TaskStage?,
    ) {
        val now = System.currentTimeMillis()
        val lastUserMessage = if (branchId != null) {
            messageDao.getLastUserMessageForBranch(conversationId, branchId)
        } else {
            messageDao.getLastUserMessage(conversationId)
        }
        lastUserMessage?.let { userMessageEntity ->
            messageDao.update(
                userMessageEntity.copy(
                    cacheHitTokens = aiAgentMessage.usage.promptCacheHitTokens,
                    tokenCount = aiAgentMessage.usage.promptCacheMissTokens,
                ),
            )
        }
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                text = aiAgentMessage.text,
                role = MessageRole.AI.toEntityRole(),
                timestamp = now,
                tokenCount = aiAgentMessage.usage.completionTokens,
                branchId = branchId,
                taskStage = taskStage?.toEntityStage(),
            ),
        )
        touchConversation(conversationId, now)
    }

    override fun observeTotalTokenCount(conversationId: String): Flow<Int> {
        return messageDao.observeMessages(conversationId).map { messages ->
            val aiTokens = messages.lastOrNull {
                it.role == MessageRole.AI.toEntityRole() && it.tokenCount != null
            }?.tokenCount ?: 0

            val lastUserMessage = messages.lastOrNull {
                it.role == MessageRole.USER.toEntityRole() && it.tokenCount != null
            }

            val lastUserTokens = (lastUserMessage?.tokenCount ?: 0) + (lastUserMessage?.cacheHitTokens ?: 0)

            aiTokens + lastUserTokens
        }
    }

    override fun observeChatCost(conversationId: String): Flow<Double> {
        return messageDao.observeMessages(conversationId).map { messages ->
            calculateChatCost(messages.map { it.toDomain() })
        }
    }

    override suspend fun ensureConversationExists(
        conversationId: String,
        mode: ConversationMode,
    ) {
        if (conversationDao.getById(conversationId) != null) return
        val now = System.currentTimeMillis()
        val title = when (mode) {
            ConversationMode.CHAT -> DEFAULT_TITLE
            ConversationMode.TASK -> DEFAULT_TASK_TITLE
        }
        conversationDao.insert(
            ConversationEntity(
                id = conversationId,
                title = title,
                createdAt = now,
                updatedAt = now,
                mode = mode.name,
            ),
        )
    }

    override suspend fun updateConversationTitle(conversationId: String, title: String) {
        conversationDao.getById(conversationId)?.let { conversation ->
            conversationDao.update(
                conversation.copy(
                    title = title,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    override suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteById(conversationId)
        persistedTextAttachmentBuilder.deleteConversationAttachments(conversationId)
    }

    private suspend fun touchConversation(conversationId: String, timestamp: Long) {
        conversationDao.getById(conversationId)?.let { conversation ->
            conversationDao.update(conversation.copy(updatedAt = timestamp))
        }
    }

    private suspend fun ChatHistoryMessage.resolveTextForApi(): ChatHistoryMessage {
        if (text.isNotBlank() || attachmentUri == null) return this
        return copy(text = persistedTextAttachmentBuilder.readStoredContent(attachmentUri))
    }

    private companion object {
        const val DEFAULT_TITLE = "Новый чат"
        const val DEFAULT_TASK_TITLE = "Новая задача"
    }
}
