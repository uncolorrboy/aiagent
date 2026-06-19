package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sapozhnikov.aiagent.data.local.dao.MessageDao
import ru.sapozhnikov.aiagent.data.local.dao.TaskArtifactDao
import ru.sapozhnikov.aiagent.data.local.dao.TaskStateDao
import ru.sapozhnikov.aiagent.data.local.mapper.toDomain
import ru.sapozhnikov.aiagent.data.local.mapper.toEntity
import ru.sapozhnikov.aiagent.data.local.mapper.toEntityStage
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.model.TaskArtifact
import ru.sapozhnikov.aiagent.domain.model.TaskStage
import ru.sapozhnikov.aiagent.domain.model.TaskState
import ru.sapozhnikov.aiagent.domain.repository.TaskRepository
import javax.inject.Inject

/** Реализация [TaskRepository] на базе Room. */
internal class TaskRepositoryImpl @Inject constructor(
    private val taskStateDao: TaskStateDao,
    private val taskArtifactDao: TaskArtifactDao,
    private val messageDao: MessageDao,
) : TaskRepository {

    override fun observeTaskState(conversationId: String): Flow<TaskState?> {
        return taskStateDao.observeTaskState(conversationId).map { it?.toDomain() }
    }

    override fun observeArtifacts(conversationId: String): Flow<List<TaskArtifact>> {
        return taskArtifactDao.observeArtifacts(conversationId).map { artifacts ->
            artifacts.map { it.toDomain() }
        }
    }

    override suspend fun getTaskState(conversationId: String): TaskState? {
        return taskStateDao.getTaskState(conversationId)?.toDomain()
    }

    override suspend fun getArtifacts(conversationId: String): List<TaskArtifact> {
        return taskArtifactDao.getArtifacts(conversationId).map { it.toDomain() }
    }

    override suspend fun initializeTask(conversationId: String) {
        if (taskStateDao.getTaskState(conversationId) != null) return
        val initialStage = TaskStage.DATA_COLLECTION
        taskStateDao.upsert(
            TaskState(
                conversationId = conversationId,
                activeStage = initialStage,
                viewingStage = initialStage,
            ).toEntity(),
        )
        messageDao.repairOrphanTaskMessages(conversationId, initialStage.toEntityStage())
    }

    override suspend fun updateTaskState(taskState: TaskState) {
        taskStateDao.upsert(taskState.toEntity())
    }

    override suspend fun saveArtifact(artifact: TaskArtifact) {
        taskArtifactDao.upsert(artifact.toEntity())
    }

    override suspend fun buildStageArtifact(conversationId: String, stage: TaskStage): String {
        val messages = messageDao.getMessagesForTaskStage(conversationId, stage.toEntityStage())
        if (messages.isEmpty()) return ""
        return messages.joinToString(separator = "\n\n") { entity ->
            val roleLabel = when (entity.role) {
                MessageRole.USER.name -> "Пользователь"
                MessageRole.AI.name -> "Ассистент"
                else -> entity.role
            }
            "$roleLabel: ${entity.text}"
        }
    }

    override suspend fun getArtifactsBeforeStage(
        conversationId: String,
        stage: TaskStage,
    ): List<TaskArtifact> {
        val stageOrder = TaskStage.entries.indexOf(stage)
        return getArtifacts(conversationId).filter { artifact ->
            TaskStage.entries.indexOf(artifact.stage) < stageOrder
        }
    }
}
