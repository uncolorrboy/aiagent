package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.ConversationBranch
import ru.sapozhnikov.aiagent.domain.repository.ConversationBranchRepository
import javax.inject.Inject

/** Use-case для управления ветками диалога. */
internal class ConversationBranchInteractor @Inject constructor(
    private val repository: ConversationBranchRepository,
) {

    /** Наблюдает за ветками диалога. */
    fun observeBranches(conversationId: String): Flow<List<ConversationBranch>> {
        return repository.observeBranches(conversationId)
    }

    /** Создаёт checkpoint и две ветки от текущего места диалога. */
    suspend fun createCheckpoint(conversationId: String): Result<Unit> {
        return repository.createCheckpoint(conversationId)
    }

    /** Переключает активную ветку. */
    suspend fun switchBranch(conversationId: String, branchId: String) {
        repository.switchBranch(conversationId, branchId)
    }
}
