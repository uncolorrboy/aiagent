package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sapozhnikov.aiagent.data.local.dao.ConversationMemoryBindingDao
import ru.sapozhnikov.aiagent.data.local.mapper.toDomain
import ru.sapozhnikov.aiagent.data.local.mapper.toEntity
import ru.sapozhnikov.aiagent.domain.model.ConversationMemorySelection
import ru.sapozhnikov.aiagent.domain.repository.ConversationMemoryRepository
import javax.inject.Inject

/** Реализация [ConversationMemoryRepository] на базе Room. */
internal class ConversationMemoryRepositoryImpl @Inject constructor(
    private val conversationMemoryBindingDao: ConversationMemoryBindingDao,
) : ConversationMemoryRepository {

    override fun observeSelection(conversationId: String): Flow<ConversationMemorySelection?> {
        return conversationMemoryBindingDao.observeByConversationId(conversationId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getSelection(conversationId: String): ConversationMemorySelection? {
        return conversationMemoryBindingDao.getByConversationId(conversationId)?.toDomain()
    }

    override suspend fun saveSelection(selection: ConversationMemorySelection) {
        conversationMemoryBindingDao.upsert(selection.toEntity())
    }
}
