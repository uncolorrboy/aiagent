package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.TaskArtifact
import ru.sapozhnikov.aiagent.domain.model.TaskStage
import ru.sapozhnikov.aiagent.domain.model.TaskState

/** Репозиторий для управления задачами в режиме TASK. */
internal interface TaskRepository {

    fun observeTaskState(conversationId: String): Flow<TaskState?>

    fun observeArtifacts(conversationId: String): Flow<List<TaskArtifact>>

    suspend fun getTaskState(conversationId: String): TaskState?

    suspend fun getArtifacts(conversationId: String): List<TaskArtifact>

    suspend fun initializeTask(conversationId: String)

    suspend fun updateTaskState(taskState: TaskState)

    suspend fun saveArtifact(artifact: TaskArtifact)

    suspend fun buildStageArtifact(conversationId: String, stage: TaskStage): String

    suspend fun getArtifactsBeforeStage(conversationId: String, stage: TaskStage): List<TaskArtifact>

    /** Удаляет артефакт и переписку этапа, с которого выполнен откат. */
    suspend fun clearStageOnRollback(conversationId: String, leftStage: TaskStage)
}
