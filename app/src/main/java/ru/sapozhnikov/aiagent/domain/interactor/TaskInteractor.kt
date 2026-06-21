package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.TaskArtifact
import ru.sapozhnikov.aiagent.domain.model.TaskStage
import ru.sapozhnikov.aiagent.domain.model.TaskStageTransitions
import ru.sapozhnikov.aiagent.domain.model.TaskState
import ru.sapozhnikov.aiagent.domain.repository.TaskRepository
import javax.inject.Inject

/** Use-case для управления задачами в режиме TASK. */
internal class TaskInteractor @Inject constructor(
    private val taskRepository: TaskRepository,
) {

    fun observeTaskState(conversationId: String): Flow<TaskState?> =
        taskRepository.observeTaskState(conversationId)

    fun observeArtifacts(conversationId: String): Flow<List<TaskArtifact>> =
        taskRepository.observeArtifacts(conversationId)

    suspend fun getTaskState(conversationId: String): TaskState? =
        taskRepository.getTaskState(conversationId)

    suspend fun initializeTask(conversationId: String) {
        taskRepository.initializeTask(conversationId)
    }

    suspend fun switchViewingStage(conversationId: String, stage: TaskStage) {
        val current = taskRepository.getTaskState(conversationId) ?: return
        taskRepository.updateTaskState(current.copy(viewingStage = stage))
    }

    /**
     * Обрабатывает переход на следующий этап: сохраняет артефакт текущего этапа
     * и обновляет active/viewing stage.
     *
     * @return новый активный этап или null, если переход невозможен
     */
    suspend fun transitionToStage(
        conversationId: String,
        targetStage: TaskStage,
    ): TaskStage? {
        val current = taskRepository.getTaskState(conversationId) ?: return null
        val activeStage = current.activeStage

        if (!TaskStageTransitions.isValidTransition(activeStage, targetStage)) return null

        if (TaskStageTransitions.isBackwardTransition(activeStage, targetStage)) {
            taskRepository.clearStageOnRollback(conversationId, activeStage)
        } else {
            saveArtifactForStage(conversationId, activeStage)
        }

        val newState = current.copy(
            activeStage = targetStage,
            viewingStage = targetStage,
        )
        taskRepository.updateTaskState(newState)
        return targetStage
    }

    suspend fun advanceToNextStage(conversationId: String): TaskStage? {
        val current = taskRepository.getTaskState(conversationId) ?: return null
        val next = current.activeStage.next() ?: return null
        return transitionToStage(conversationId, next)
    }

    suspend fun revertToPreviousStage(conversationId: String): TaskStage? {
        val current = taskRepository.getTaskState(conversationId) ?: return null
        val previous = current.activeStage.previous() ?: return null
        return transitionToStage(conversationId, previous)
    }

    suspend fun getArtifactsBeforeStage(conversationId: String, stage: TaskStage): List<TaskArtifact> {
        return taskRepository.getArtifactsBeforeStage(conversationId, stage)
    }

    private suspend fun saveArtifactForStage(conversationId: String, stage: TaskStage) {
        val content = taskRepository.buildStageArtifact(conversationId, stage)
        if (content.isBlank()) return
        taskRepository.saveArtifact(
            TaskArtifact(
                conversationId = conversationId,
                stage = stage,
                content = content,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

}
