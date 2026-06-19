package ru.sapozhnikov.aiagent.data.local.mapper

import ru.sapozhnikov.aiagent.data.local.entity.TaskArtifactEntity
import ru.sapozhnikov.aiagent.data.local.entity.TaskStateEntity
import ru.sapozhnikov.aiagent.domain.model.TaskArtifact
import ru.sapozhnikov.aiagent.domain.model.TaskStage
import ru.sapozhnikov.aiagent.domain.model.TaskState

internal fun TaskStateEntity.toDomain(): TaskState = TaskState(
    conversationId = conversationId,
    activeStage = TaskStage.valueOf(activeStage),
    viewingStage = TaskStage.valueOf(viewingStage),
)

internal fun TaskState.toEntity(): TaskStateEntity = TaskStateEntity(
    conversationId = conversationId,
    activeStage = activeStage.name,
    viewingStage = viewingStage.name,
)

internal fun TaskArtifactEntity.toDomain(): TaskArtifact = TaskArtifact(
    conversationId = conversationId,
    stage = TaskStage.valueOf(stage),
    content = content,
    createdAt = createdAt,
)

internal fun TaskArtifact.toEntity(): TaskArtifactEntity = TaskArtifactEntity(
    conversationId = conversationId,
    stage = stage.name,
    content = content,
    createdAt = createdAt,
)

internal fun TaskStage.toEntityStage(): String = name

internal fun String?.toTaskStageOrNull(): TaskStage? = this?.let { runCatching { TaskStage.valueOf(it) }.getOrNull() }
