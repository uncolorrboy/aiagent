package ru.sapozhnikov.aiagent.data.repository

import ru.sapozhnikov.aiagent.data.local.FactsJsonSerializer
import ru.sapozhnikov.aiagent.data.local.dao.ConversationFactsDao
import ru.sapozhnikov.aiagent.data.local.mapper.toDomain
import ru.sapozhnikov.aiagent.data.local.mapper.toEntity
import ru.sapozhnikov.aiagent.domain.model.ConversationFacts
import ru.sapozhnikov.aiagent.domain.repository.ConversationFactsRepository
import javax.inject.Inject

/** Реализация [ConversationFactsRepository] на базе Room. */
internal class ConversationFactsRepositoryImpl @Inject constructor(
    private val conversationFactsDao: ConversationFactsDao,
    private val factsJsonSerializer: FactsJsonSerializer,
) : ConversationFactsRepository {

    override suspend fun getFacts(conversationId: String): ConversationFacts? {
        val entity = conversationFactsDao.getFacts(conversationId) ?: return null
        return entity.toDomain(factsJsonSerializer.fromJson(entity.factsJson))
    }

    override suspend fun saveFacts(facts: ConversationFacts) {
        conversationFactsDao.upsert(
            facts.toEntity(factsJsonSerializer.toJson(facts.facts)),
        )
    }
}
