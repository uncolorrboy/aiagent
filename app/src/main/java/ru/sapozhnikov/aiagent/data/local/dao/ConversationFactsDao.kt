package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.sapozhnikov.aiagent.data.local.entity.ConversationFactsEntity

/** DAO для работы с блоком фактов диалога. */
@Dao
internal interface ConversationFactsDao {

    /** Возвращает факты диалога. */
    @Query("SELECT * FROM conversation_facts WHERE conversationId = :conversationId LIMIT 1")
    suspend fun getFacts(conversationId: String): ConversationFactsEntity?

    /** Сохраняет или обновляет факты диалога. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(facts: ConversationFactsEntity)
}
