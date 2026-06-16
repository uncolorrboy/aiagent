package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sapozhnikov.aiagent.data.local.dao.ProfileMemoryDao
import ru.sapozhnikov.aiagent.data.local.entity.ProfileMemoryEntity
import ru.sapozhnikov.aiagent.data.local.mapper.toDomain
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance
import ru.sapozhnikov.aiagent.domain.repository.ProfileMemoryRepository
import java.util.UUID
import javax.inject.Inject

/** Реализация [ProfileMemoryRepository] на базе Room. */
internal class ProfileMemoryRepositoryImpl @Inject constructor(
    private val profileMemoryDao: ProfileMemoryDao,
) : ProfileMemoryRepository {

    override fun observeInstances(): Flow<List<MemoryInstance>> {
        return profileMemoryDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): MemoryInstance? {
        return profileMemoryDao.getById(id)?.toDomain()
    }

    override suspend fun create(name: String, text: String): MemoryInstance {
        val entity = ProfileMemoryEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            text = text.trim(),
            createdAt = System.currentTimeMillis(),
        )
        profileMemoryDao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun update(instance: MemoryInstance) {
        val existing = profileMemoryDao.getById(instance.id) ?: return
        profileMemoryDao.update(
            existing.copy(
                name = instance.name.trim(),
                text = instance.text.trim(),
            ),
        )
    }

    override suspend fun delete(id: String) {
        profileMemoryDao.deleteById(id)
    }
}
