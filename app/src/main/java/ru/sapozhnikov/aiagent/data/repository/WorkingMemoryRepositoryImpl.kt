package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sapozhnikov.aiagent.data.local.dao.WorkingMemoryDao
import ru.sapozhnikov.aiagent.data.local.entity.WorkingMemoryEntity
import ru.sapozhnikov.aiagent.data.local.mapper.toDomain
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance
import ru.sapozhnikov.aiagent.domain.repository.WorkingMemoryRepository
import java.util.UUID
import javax.inject.Inject

/** Реализация [WorkingMemoryRepository] на базе Room. */
internal class WorkingMemoryRepositoryImpl @Inject constructor(
    private val workingMemoryDao: WorkingMemoryDao,
) : WorkingMemoryRepository {

    override fun observeInstances(): Flow<List<MemoryInstance>> {
        return workingMemoryDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): MemoryInstance? {
        return workingMemoryDao.getById(id)?.toDomain()
    }

    override suspend fun create(name: String, text: String): MemoryInstance {
        val entity = WorkingMemoryEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            text = text.trim(),
            createdAt = System.currentTimeMillis(),
        )
        workingMemoryDao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun update(instance: MemoryInstance) {
        val existing = workingMemoryDao.getById(instance.id) ?: return
        workingMemoryDao.update(
            existing.copy(
                name = instance.name.trim(),
                text = instance.text.trim(),
            ),
        )
    }

    override suspend fun delete(id: String) {
        workingMemoryDao.deleteById(id)
    }
}
