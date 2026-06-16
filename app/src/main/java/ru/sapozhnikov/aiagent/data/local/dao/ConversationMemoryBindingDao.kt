package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.ConversationMemoryBindingEntity

/** DAO для привязки памяти к диалогам. */
@Dao
internal interface ConversationMemoryBindingDao {

    @Query("SELECT * FROM conversation_memory_bindings WHERE conversationId = :conversationId LIMIT 1")
    fun observeByConversationId(conversationId: String): Flow<ConversationMemoryBindingEntity?>

    @Query("SELECT * FROM conversation_memory_bindings WHERE conversationId = :conversationId LIMIT 1")
    suspend fun getByConversationId(conversationId: String): ConversationMemoryBindingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(binding: ConversationMemoryBindingEntity)
}
