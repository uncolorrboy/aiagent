package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.AssistantInvariantEntity

/** DAO для инвариантов ассистента. */
@Dao
internal interface AssistantInvariantDao {

    @Query("SELECT * FROM assistant_invariants ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<AssistantInvariantEntity>>

    @Query("SELECT * FROM assistant_invariants WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AssistantInvariantEntity?

    @Query("SELECT * FROM assistant_invariants WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<AssistantInvariantEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AssistantInvariantEntity)

    @Update
    suspend fun update(entity: AssistantInvariantEntity)

    @Query("DELETE FROM assistant_invariants WHERE id = :id")
    suspend fun deleteById(id: String)
}
