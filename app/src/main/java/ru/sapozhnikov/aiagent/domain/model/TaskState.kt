package ru.sapozhnikov.aiagent.domain.model

/**
 * Состояние задачи в диалоге.
 *
 * @property conversationId идентификатор диалога
 * @property activeStage текущий активный этап (куда пишутся новые сообщения)
 * @property viewingStage этап, переписку которого просматривает пользователь
 */
internal data class TaskState(
    val conversationId: String,
    val activeStage: TaskStage,
    val viewingStage: TaskStage,
)
