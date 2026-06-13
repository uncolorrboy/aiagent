package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.MessageEntity

/** DAO для работы с сообщениями чата. */
@Dao
internal interface MessageDao {

    /** Наблюдает за сообщениями диалога в хронологическом порядке. */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessages(conversationId: String): Flow<List<MessageEntity>>

    /** Наблюдает за сообщениями диалога с учётом активной ветки. */
    @Query(
        "SELECT * FROM messages WHERE conversationId = :conversationId " +
            "AND (branchId IS NULL OR branchId = :activeBranchId) ORDER BY timestamp ASC",
    )
    fun observeMessagesForBranch(conversationId: String, activeBranchId: String): Flow<List<MessageEntity>>

    /** Возвращает все сообщения диалога. */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessages(conversationId: String): List<MessageEntity>

    /** Возвращает сообщения диалога с учётом активной ветки. */
    @Query(
        "SELECT * FROM messages WHERE conversationId = :conversationId " +
            "AND (branchId IS NULL OR branchId = :activeBranchId) ORDER BY timestamp ASC",
    )
    suspend fun getMessagesForBranch(conversationId: String, activeBranchId: String): List<MessageEntity>

    /** Возвращает количество сообщений в диалоге. */
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    suspend fun getMessageCount(conversationId: String): Int

    /** Вставляет новое сообщение. */
    @Insert
    suspend fun insert(message: MessageEntity)

    /** Возвращает последнее пользовательское сообщение в диалоге. */
    @Query(
        "SELECT * FROM messages WHERE conversationId = :conversationId AND role = 'USER' " +
            "ORDER BY timestamp DESC LIMIT 1",
    )
    suspend fun getLastUserMessage(conversationId: String): MessageEntity?

    /** Возвращает последнее пользовательское сообщение в ветке. */
    @Query(
        "SELECT * FROM messages WHERE conversationId = :conversationId AND role = 'USER' " +
            "AND (branchId IS NULL OR branchId = :activeBranchId) ORDER BY timestamp DESC LIMIT 1",
    )
    suspend fun getLastUserMessageForBranch(
        conversationId: String,
        activeBranchId: String?,
    ): MessageEntity?

    /** Возвращает последнее сообщение диалога. */
    @Query(
        "SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1",
    )
    suspend fun getLastMessage(conversationId: String): MessageEntity?

    /** Обновляет существующее сообщение. */
    @Update
    suspend fun update(message: MessageEntity)
}
