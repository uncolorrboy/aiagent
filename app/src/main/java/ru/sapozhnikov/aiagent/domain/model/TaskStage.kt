package ru.sapozhnikov.aiagent.domain.model

/**
 * Этап задачи в конечном автомате.
 *
 * Порядок: DATA_COLLECTION → PLANNING → EXECUTION → VALIDATION → DONE.
 */
internal enum class TaskStage {
    DATA_COLLECTION,
    PLANNING,
    EXECUTION,
    VALIDATION,
    DONE,
    ;

    /** Следующий этап или null, если текущий — [DONE]. */
    fun next(): TaskStage? = when (this) {
        DATA_COLLECTION -> PLANNING
        PLANNING -> EXECUTION
        EXECUTION -> VALIDATION
        VALIDATION -> DONE
        DONE -> null
    }

    /** Предыдущий этап или null, если текущий — [DATA_COLLECTION]. */
    fun previous(): TaskStage? = when (this) {
        DATA_COLLECTION -> null
        PLANNING -> DATA_COLLECTION
        EXECUTION -> PLANNING
        VALIDATION -> EXECUTION
        DONE -> VALIDATION
    }

    /** Все этапы, кроме [DONE], для отображения вкладок. */
    companion object {
        val navigableStages: List<TaskStage> = entries.filter { it != DONE }
    }
}

/** Текст кнопки перехода на следующий этап (инициируется пользователем). */
internal fun TaskStage.advanceActionLabel(): String? = when (this) {
    TaskStage.DATA_COLLECTION -> "Данные собраны → Планирование"
    TaskStage.PLANNING -> "Утвердить план → Выполнение"
    TaskStage.EXECUTION -> "Работа завершена → Проверка"
    TaskStage.VALIDATION -> "Завершить задачу"
    TaskStage.DONE -> null
}

/** Короткое имя этапа для вкладок. */
internal fun TaskStage.shortName(): String = when (this) {
    TaskStage.DATA_COLLECTION -> "Сбор"
    TaskStage.PLANNING -> "План"
    TaskStage.EXECUTION -> "Работа"
    TaskStage.VALIDATION -> "Проверка"
    TaskStage.DONE -> "Готово"
}

/** Полное имя этапа для UI. */
internal fun TaskStage.displayName(): String = when (this) {
    TaskStage.DATA_COLLECTION -> "Сбор данных"
    TaskStage.PLANNING -> "Планирование"
    TaskStage.EXECUTION -> "Выполнение"
    TaskStage.VALIDATION -> "Валидация"
    TaskStage.DONE -> "Завершено"
}
