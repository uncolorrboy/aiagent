package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.ConversationEntity

/** DAO для работы с диалогами (чатами). */
@Dao
internal interface ConversationDao {

    /** Наблюдает за списком диалогов, отсортированных по дате обновления. */
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    /** Возвращает диалог по идентификатору. */
    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): ConversationEntity?

    /** Создаёт новый диалог (игнорирует конфликт по id). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(conversation: ConversationEntity)

    /** Обновляет существующий диалог. */
    @Update
    suspend fun update(conversation: ConversationEntity)

    /** Удаляет диалог по идентификатору. */
    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: String)
}
