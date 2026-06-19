package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.sapozhnikov.aiagent.domain.model.AssistantInvariant
import ru.sapozhnikov.aiagent.domain.repository.AssistantInvariantRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationInvariantBindingRepository
import javax.inject.Inject

/** Use-case для управления инвариантами ассистента. */
internal class InvariantInteractor @Inject constructor(
    private val assistantInvariantRepository: AssistantInvariantRepository,
    private val conversationInvariantBindingRepository: ConversationInvariantBindingRepository,
) {

    fun observeAllInvariants(): Flow<List<AssistantInvariant>> {
        return assistantInvariantRepository.observeAll()
    }

    suspend fun getInvariantById(id: String): AssistantInvariant? {
        return assistantInvariantRepository.getById(id)
    }

    suspend fun createInvariant(
        name: String,
        text: String,
    ): AssistantInvariant {
        return assistantInvariantRepository.create(name, text)
    }

    suspend fun updateInvariant(invariant: AssistantInvariant) {
        assistantInvariantRepository.update(invariant)
    }

    suspend fun deleteInvariant(id: String) {
        assistantInvariantRepository.delete(id)
    }

    fun observeConversationInvariantIds(conversationId: String): Flow<List<String>> {
        return conversationInvariantBindingRepository.observeInvariantIds(conversationId)
    }

    fun observeConversationInvariants(conversationId: String): Flow<List<AssistantInvariant>> {
        return combine(
            assistantInvariantRepository.observeAll(),
            conversationInvariantBindingRepository.observeInvariantIds(conversationId),
        ) { allInvariants, selectedIds ->
            val selectedIdSet = selectedIds.toSet()
            allInvariants.filter { it.id in selectedIdSet }
        }
    }

    suspend fun getInvariantsForApi(conversationId: String): List<AssistantInvariant> {
        val selectedIds = conversationInvariantBindingRepository.getInvariantIds(conversationId)
        if (selectedIds.isEmpty()) return emptyList()
        return assistantInvariantRepository.getByIds(selectedIds)
    }

    suspend fun saveConversationInvariantIds(conversationId: String, invariantIds: List<String>) {
        conversationInvariantBindingRepository.saveInvariantIds(conversationId, invariantIds)
    }
}
