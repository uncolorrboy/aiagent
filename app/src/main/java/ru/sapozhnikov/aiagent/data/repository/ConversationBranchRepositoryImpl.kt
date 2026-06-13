package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sapozhnikov.aiagent.data.local.dao.ConversationBranchDao
import ru.sapozhnikov.aiagent.data.local.dao.MessageDao
import ru.sapozhnikov.aiagent.data.local.entity.ConversationBranchEntity
import ru.sapozhnikov.aiagent.data.local.mapper.toDomain
import ru.sapozhnikov.aiagent.domain.model.ConversationBranch
import ru.sapozhnikov.aiagent.domain.repository.ConversationBranchRepository
import java.util.UUID
import javax.inject.Inject

/** Реализация [ConversationBranchRepository] на базе Room. */
internal class ConversationBranchRepositoryImpl @Inject constructor(
    private val conversationBranchDao: ConversationBranchDao,
    private val messageDao: MessageDao,
) : ConversationBranchRepository {

    override fun observeBranches(conversationId: String): Flow<List<ConversationBranch>> {
        return conversationBranchDao.observeBranches(conversationId).map { branches ->
            branches.map { it.toDomain() }
        }
    }

    override suspend fun getActiveBranch(conversationId: String): ConversationBranch? {
        return conversationBranchDao.getActiveBranch(conversationId)?.toDomain()
    }

    override suspend fun createCheckpoint(conversationId: String): Result<Unit> {
        val lastMessage = messageDao.getLastMessage(conversationId)
            ?: return Result.failure(IllegalStateException("Нет сообщений для создания checkpoint"))

        val existingBranches = conversationBranchDao.getBranches(conversationId)
        if (existingBranches.isNotEmpty()) {
            return Result.failure(IllegalStateException("Checkpoint уже создан"))
        }

        val branchAId = UUID.randomUUID().toString()
        val branchBId = UUID.randomUUID().toString()
        conversationBranchDao.createBranchesWithCheckpoint(
            conversationId = conversationId,
            checkpointMessageId = lastMessage.id,
            branchA = ConversationBranchEntity(
                id = branchAId,
                conversationId = conversationId,
                name = "Ветка A",
                checkpointMessageId = lastMessage.id,
                isActive = true,
            ),
            branchB = ConversationBranchEntity(
                id = branchBId,
                conversationId = conversationId,
                name = "Ветка B",
                checkpointMessageId = lastMessage.id,
                isActive = false,
            ),
        )
        return Result.success(Unit)
    }

    override suspend fun switchBranch(conversationId: String, branchId: String) {
        conversationBranchDao.setActiveBranch(conversationId, branchId)
    }
}
