package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance

/** Репозиторий экземпляров рабочей памяти. */
internal interface WorkingMemoryRepository {

    fun observeInstances(): Flow<List<MemoryInstance>>

    suspend fun getById(id: String): MemoryInstance?

    suspend fun create(name: String, text: String): MemoryInstance

    suspend fun update(instance: MemoryInstance)

    suspend fun delete(id: String)
}
