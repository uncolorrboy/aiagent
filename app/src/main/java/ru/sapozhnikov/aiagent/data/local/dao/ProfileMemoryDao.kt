package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.ProfileMemoryEntity

/** DAO для экземпляров долговременной памяти. */
@Dao
internal interface ProfileMemoryDao {

    @Query("SELECT * FROM profile_memory ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<ProfileMemoryEntity>>

    @Query("SELECT * FROM profile_memory WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProfileMemoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ProfileMemoryEntity)

    @Update
    suspend fun update(entity: ProfileMemoryEntity)

    @Query("DELETE FROM profile_memory WHERE id = :id")
    suspend fun deleteById(id: String)
}
