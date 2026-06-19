package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sapozhnikov.aiagent.data.local.dao.AssistantInvariantDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationInvariantBindingDao
import ru.sapozhnikov.aiagent.data.local.mapper.toDomain
import ru.sapozhnikov.aiagent.data.local.mapper.toEntity
import ru.sapozhnikov.aiagent.domain.model.AssistantInvariant
import ru.sapozhnikov.aiagent.domain.repository.AssistantInvariantRepository
import java.util.UUID
import javax.inject.Inject

/** Реализация [AssistantInvariantRepository] на базе Room. */
internal class AssistantInvariantRepositoryImpl @Inject constructor(
    private val assistantInvariantDao: AssistantInvariantDao,
    private val conversationInvariantBindingDao: ConversationInvariantBindingDao,
) : AssistantInvariantRepository {

    override fun observeAll(): Flow<List<AssistantInvariant>> {
        return assistantInvariantDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): AssistantInvariant? {
        return assistantInvariantDao.getById(id)?.toDomain()
    }

    override suspend fun getByIds(ids: Collection<String>): List<AssistantInvariant> {
        if (ids.isEmpty()) return emptyList()
        return assistantInvariantDao.getByIds(ids.toList()).map { it.toDomain() }
    }

    override suspend fun create(name: String, text: String): AssistantInvariant {
        val entity = AssistantInvariant(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            text = text.trim(),
        ).toEntity(createdAt = System.currentTimeMillis())
        assistantInvariantDao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun update(invariant: AssistantInvariant) {
        val existing = assistantInvariantDao.getById(invariant.id) ?: return
        assistantInvariantDao.update(
            existing.copy(
                name = invariant.name.trim(),
                text = invariant.text.trim(),
            ),
        )
    }

    override suspend fun delete(id: String) {
        conversationInvariantBindingDao.deleteByInvariantId(id)
        assistantInvariantDao.deleteById(id)
    }
}
