package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.sapozhnikov.aiagent.data.local.entity.ConversationSummaryEntity

/** DAO для работы с резюме диалогов. */
@Dao
internal interface ConversationSummaryDao {

    /** Возвращает резюме диалога по идентификатору. */
    @Query("SELECT * FROM conversation_summaries WHERE conversationId = :conversationId")
    suspend fun getByConversationId(conversationId: String): ConversationSummaryEntity?

    /** Сохраняет или заменяет резюме диалога. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: ConversationSummaryEntity)
}
