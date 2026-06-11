package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.MessageEntity

@Dao
internal interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessages(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessages(conversationId: String): List<MessageEntity>

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun getMessageCount(conversationId: String): Int

    @Insert
    suspend fun insert(message: MessageEntity)

    @Query(
        "SELECT * FROM messages WHERE conversationId = :conversationId AND role = 'USER' " +
            "ORDER BY timestamp DESC LIMIT 1",
    )
    suspend fun getLastUserMessage(conversationId: String): MessageEntity?

    @Update
    suspend fun update(message: MessageEntity)
}
