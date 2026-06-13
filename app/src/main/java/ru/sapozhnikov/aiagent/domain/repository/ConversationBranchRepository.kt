package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.ConversationBranch

/** Репозиторий для управления ветками диалога. */
internal interface ConversationBranchRepository {

    /** Наблюдает за ветками диалога. */
    fun observeBranches(conversationId: String): Flow<List<ConversationBranch>>

    /** Возвращает активную ветку или null. */
    suspend fun getActiveBranch(conversationId: String): ConversationBranch?

    /** Создаёт checkpoint и две ветки от последнего сообщения. */
    suspend fun createCheckpoint(conversationId: String): Result<Unit>

    /** Переключает активную ветку. */
    suspend fun switchBranch(conversationId: String, branchId: String)
}
