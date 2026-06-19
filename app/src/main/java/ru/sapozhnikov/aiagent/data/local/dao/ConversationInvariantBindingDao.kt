package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.ConversationInvariantBindingEntity

/** DAO для привязки инвариантов к диалогам. */
@Dao
internal interface ConversationInvariantBindingDao {

    @Query(
        "SELECT invariantId FROM conversation_invariant_bindings " +
            "WHERE conversationId = :conversationId ORDER BY invariantId ASC",
    )
    fun observeInvariantIds(conversationId: String): Flow<List<String>>

    @Query(
        "SELECT invariantId FROM conversation_invariant_bindings " +
            "WHERE conversationId = :conversationId ORDER BY invariantId ASC",
    )
    suspend fun getInvariantIds(conversationId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bindings: List<ConversationInvariantBindingEntity>)

    @Query("DELETE FROM conversation_invariant_bindings WHERE conversationId = :conversationId")
    suspend fun deleteByConversationId(conversationId: String)

    @Query("DELETE FROM conversation_invariant_bindings WHERE invariantId = :invariantId")
    suspend fun deleteByInvariantId(invariantId: String)

    @Transaction
    suspend fun replaceForConversation(
        conversationId: String,
        bindings: List<ConversationInvariantBindingEntity>,
    ) {
        deleteByConversationId(conversationId)
        if (bindings.isNotEmpty()) {
            insertAll(bindings)
        }
    }
}
