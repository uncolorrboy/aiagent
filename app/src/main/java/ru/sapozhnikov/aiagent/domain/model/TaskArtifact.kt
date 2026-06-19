package ru.sapozhnikov.aiagent.domain.model

/**
 * Артефакт — результат завершённого этапа задачи.
 *
 * Передаётся следующим агентам как контекст.
 *
 * @property conversationId идентификатор диалога
 * @property stage этап, к которому относится артефакт
 * @property content текстовое содержимое (сводка переписки этапа)
 * @property createdAt время создания (мс)
 */
internal data class TaskArtifact(
    val conversationId: String,
    val stage: TaskStage,
    val content: String,
    val createdAt: Long,
)
