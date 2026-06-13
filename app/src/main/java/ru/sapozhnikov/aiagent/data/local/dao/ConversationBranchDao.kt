package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.ConversationBranchEntity

/** DAO для работы с ветками диалога. */
@Dao
internal interface ConversationBranchDao {

    /** Наблюдает за ветками диалога. */
    @Query(
        "SELECT * FROM conversation_branches WHERE conversationId = :conversationId ORDER BY name ASC",
    )
    fun observeBranches(conversationId: String): Flow<List<ConversationBranchEntity>>

    /** Возвращает все ветки диалога. */
    @Query(
        "SELECT * FROM conversation_branches WHERE conversationId = :conversationId ORDER BY name ASC",
    )
    suspend fun getBranches(conversationId: String): List<ConversationBranchEntity>

    /** Возвращает активную ветку диалога. */
    @Query(
        "SELECT * FROM conversation_branches WHERE conversationId = :conversationId AND isActive = 1 LIMIT 1",
    )
    suspend fun getActiveBranch(conversationId: String): ConversationBranchEntity?

    /** Вставляет ветки. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(branches: List<ConversationBranchEntity>)

    /** Обновляет флаг активности ветки. */
    @Query(
        "UPDATE conversation_branches SET isActive = CASE WHEN id = :branchId THEN 1 ELSE 0 END " +
            "WHERE conversationId = :conversationId",
    )
    suspend fun setActiveBranch(conversationId: String, branchId: String)

    /** Удаляет все ветки диалога. */
    @Query("DELETE FROM conversation_branches WHERE conversationId = :conversationId")
    suspend fun deleteByConversationId(conversationId: String)

    /** Атомарно создаёт две ветки и активирует первую. */
    @Transaction
    suspend fun createBranchesWithCheckpoint(
        conversationId: String,
        checkpointMessageId: Long,
        branchA: ConversationBranchEntity,
        branchB: ConversationBranchEntity,
    ) {
        deleteByConversationId(conversationId)
        insertAll(listOf(branchA, branchB))
        setActiveBranch(conversationId, branchA.id)
    }
}
