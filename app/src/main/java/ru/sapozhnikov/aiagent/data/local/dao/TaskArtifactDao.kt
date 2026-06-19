package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.TaskArtifactEntity

/** DAO для артефактов задачи. */
@Dao
internal interface TaskArtifactDao {

    @Query("SELECT * FROM task_artifacts WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun observeArtifacts(conversationId: String): Flow<List<TaskArtifactEntity>>

    @Query("SELECT * FROM task_artifacts WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    suspend fun getArtifacts(conversationId: String): List<TaskArtifactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(artifact: TaskArtifactEntity)
}
