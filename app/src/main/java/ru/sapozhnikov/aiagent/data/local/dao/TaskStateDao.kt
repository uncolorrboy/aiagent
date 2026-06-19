package ru.sapozhnikov.aiagent.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.entity.TaskStateEntity

/** DAO для состояния задачи. */
@Dao
internal interface TaskStateDao {

    @Query("SELECT * FROM task_states WHERE conversationId = :conversationId")
    fun observeTaskState(conversationId: String): Flow<TaskStateEntity?>

    @Query("SELECT * FROM task_states WHERE conversationId = :conversationId")
    suspend fun getTaskState(conversationId: String): TaskStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(taskState: TaskStateEntity)
}
