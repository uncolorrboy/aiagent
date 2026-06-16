package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.WorkingMemoryEntity

/** DAO для экземпляров рабочей памяти. */
@Dao
internal interface WorkingMemoryDao {

    @Query("SELECT * FROM working_memory ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<WorkingMemoryEntity>>

    @Query("SELECT * FROM working_memory WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): WorkingMemoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: WorkingMemoryEntity)

    @Update
    suspend fun update(entity: WorkingMemoryEntity)

    @Query("DELETE FROM working_memory WHERE id = :id")
    suspend fun deleteById(id: String)
}
