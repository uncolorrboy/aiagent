package ru.sapozhnikov.aiagent.domain.model

/**
 * Проверяет, принадлежит ли сообщение указанному этапу задачи.
 * Сообщения без [taskStage] (legacy) считаются принадлежащими [TaskStage.DATA_COLLECTION].
 */
internal fun ChatHistoryMessage.belongsToTaskStage(stage: TaskStage): Boolean {
    val messageStage = taskStage ?: TaskStage.DATA_COLLECTION
    return messageStage == stage
}
