package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.AssistantInvariant

/** Репозиторий глобальных инвариантов ассистента. */
internal interface AssistantInvariantRepository {

    fun observeAll(): Flow<List<AssistantInvariant>>

    suspend fun getById(id: String): AssistantInvariant?

    suspend fun getByIds(ids: Collection<String>): List<AssistantInvariant>

    suspend fun create(name: String, text: String): AssistantInvariant

    suspend fun update(invariant: AssistantInvariant)

    suspend fun delete(id: String)
}
