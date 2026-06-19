package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.dao.ConversationInvariantBindingDao
import ru.sapozhnikov.aiagent.data.local.entity.ConversationInvariantBindingEntity
import ru.sapozhnikov.aiagent.domain.repository.ConversationInvariantBindingRepository
import javax.inject.Inject

/** Реализация [ConversationInvariantBindingRepository] на базе Room. */
internal class ConversationInvariantBindingRepositoryImpl @Inject constructor(
    private val conversationInvariantBindingDao: ConversationInvariantBindingDao,
) : ConversationInvariantBindingRepository {

    override fun observeInvariantIds(conversationId: String): Flow<List<String>> {
        return conversationInvariantBindingDao.observeInvariantIds(conversationId)
    }

    override suspend fun getInvariantIds(conversationId: String): List<String> {
        return conversationInvariantBindingDao.getInvariantIds(conversationId)
    }

    override suspend fun saveInvariantIds(conversationId: String, invariantIds: List<String>) {
        val bindings = invariantIds.distinct().map { invariantId ->
            ConversationInvariantBindingEntity(
                conversationId = conversationId,
                invariantId = invariantId,
            )
        }
        conversationInvariantBindingDao.replaceForConversation(conversationId, bindings)
    }

    override suspend fun deleteByConversationId(conversationId: String) {
        conversationInvariantBindingDao.deleteByConversationId(conversationId)
    }
}
