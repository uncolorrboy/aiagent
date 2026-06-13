package ru.sapozhnikov.aiagent.data.repository

import ru.sapozhnikov.aiagent.data.local.dao.ConversationSummaryDao
import ru.sapozhnikov.aiagent.data.local.mapper.toDomain
import ru.sapozhnikov.aiagent.data.local.mapper.toEntity
import ru.sapozhnikov.aiagent.domain.model.ConversationSummary
import ru.sapozhnikov.aiagent.domain.repository.ConversationSummaryRepository
import javax.inject.Inject

internal class ConversationSummaryRepositoryImpl @Inject constructor(
    private val conversationSummaryDao: ConversationSummaryDao,
) : ConversationSummaryRepository {

    override suspend fun getSummary(conversationId: String): ConversationSummary? {
        return conversationSummaryDao.getByConversationId(conversationId)?.toDomain()
    }

    override suspend fun saveSummary(summary: ConversationSummary) {
        conversationSummaryDao.upsert(summary.toEntity())
    }
}
