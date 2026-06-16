package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.ConversationMemorySelection
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance
import ru.sapozhnikov.aiagent.domain.repository.ConversationMemoryRepository
import ru.sapozhnikov.aiagent.domain.repository.ProfileMemoryRepository
import ru.sapozhnikov.aiagent.domain.repository.WorkingMemoryRepository
import javax.inject.Inject

/** Use-case для управления памятью ассистента. */
internal class MemoryInteractor @Inject constructor(
    private val workingMemoryRepository: WorkingMemoryRepository,
    private val profileMemoryRepository: ProfileMemoryRepository,
    private val conversationMemoryRepository: ConversationMemoryRepository,
) {

    fun observeWorkingMemoryInstances(): Flow<List<MemoryInstance>> {
        return workingMemoryRepository.observeInstances()
    }

    fun observeProfileMemoryInstances(): Flow<List<MemoryInstance>> {
        return profileMemoryRepository.observeInstances()
    }

    suspend fun createWorkingMemoryInstance(name: String, text: String): MemoryInstance {
        return workingMemoryRepository.create(name, text)
    }

    suspend fun createProfileMemoryInstance(name: String, text: String): MemoryInstance {
        return profileMemoryRepository.create(name, text)
    }

    suspend fun updateWorkingMemoryInstance(instance: MemoryInstance) {
        workingMemoryRepository.update(instance)
    }

    suspend fun updateProfileMemoryInstance(instance: MemoryInstance) {
        profileMemoryRepository.update(instance)
    }

    suspend fun deleteWorkingMemoryInstance(id: String) {
        workingMemoryRepository.delete(id)
    }

    suspend fun deleteProfileMemoryInstance(id: String) {
        profileMemoryRepository.delete(id)
    }

    fun observeConversationMemorySelection(conversationId: String): Flow<ConversationMemorySelection?> {
        return conversationMemoryRepository.observeSelection(conversationId)
    }

    suspend fun getConversationMemorySelection(conversationId: String): ConversationMemorySelection? {
        return conversationMemoryRepository.getSelection(conversationId)
    }

    suspend fun saveConversationMemorySelection(selection: ConversationMemorySelection) {
        conversationMemoryRepository.saveSelection(selection)
    }

    suspend fun getWorkingMemoryForApi(id: String): MemoryInstance? {
        return workingMemoryRepository.getById(id)
    }

    suspend fun getProfileMemoryForApi(id: String): MemoryInstance? {
        return profileMemoryRepository.getById(id)
    }
}
