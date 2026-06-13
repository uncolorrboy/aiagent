package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.sapozhnikov.aiagent.data.local.entity.ConversationSummaryEntity

@Dao
internal interface ConversationSummaryDao {

    @Query("SELECT * FROM conversation_summaries WHERE conversationId = :conversationId")
    suspend fun getByConversationId(conversationId: String): ConversationSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: ConversationSummaryEntity)
}
