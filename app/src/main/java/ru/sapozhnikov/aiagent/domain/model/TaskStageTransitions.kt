package ru.sapozhnikov.aiagent.domain.model

/**
 * Правила переходов между этапами задачи и разбор маркеров ассистента.
 *
 * Маркер: `[[TRANSITION:STAGE]]`, где STAGE — имя [TaskStage].
 */
internal object TaskStageTransitions {

    private val TRANSITION_MARKER_REGEX =
        Regex("""\[\[TRANSITION:(\w+)\]\]""", RegexOption.IGNORE_CASE)

    /** Допустимые переходы: текущий этап → множество целевых этапов (вперёд и назад на один шаг). */
    private val allowedTransitions: Map<TaskStage, Set<TaskStage>> = mapOf(
        TaskStage.DATA_COLLECTION to setOf(TaskStage.PLANNING),
        TaskStage.PLANNING to setOf(TaskStage.EXECUTION, TaskStage.DATA_COLLECTION),
        TaskStage.EXECUTION to setOf(TaskStage.VALIDATION, TaskStage.PLANNING),
        TaskStage.VALIDATION to setOf(TaskStage.DONE, TaskStage.EXECUTION),
        TaskStage.DONE to setOf(TaskStage.VALIDATION),
    )

    /** Можно ли перейти с [from] на [to]. */
    fun isValidTransition(from: TaskStage, to: TaskStage): Boolean {
        return allowedTransitions[from]?.contains(to) == true
    }

    /** Переход назад на предыдущий этап (индекс целевого этапа меньше текущего). */
    fun isBackwardTransition(from: TaskStage, to: TaskStage): Boolean {
        return isValidTransition(from, to) &&
            TaskStage.entries.indexOf(to) < TaskStage.entries.indexOf(from)
    }

    /** Следующий этап по умолчанию или null. */
    fun defaultNextStage(from: TaskStage): TaskStage? = from.next()

    /** Предыдущий этап для возврата или null. */
    fun defaultPreviousStage(from: TaskStage): TaskStage? = from.previous()

    /** Все допустимые целевые этапы с текущего. */
    fun allowedTargetStages(from: TaskStage): Set<TaskStage> =
        allowedTransitions[from].orEmpty()

    /** Извлекает целевой этап из текста ответа ассистента. */
    fun parseTransitionMarker(text: String): TaskStage? {
        val stageName = TRANSITION_MARKER_REGEX.find(text)?.groupValues?.getOrNull(1) ?: return null
        return runCatching { TaskStage.valueOf(stageName.uppercase()) }.getOrNull()
    }

    /** Удаляет маркеры перехода из текста перед сохранением в историю. */
    fun stripTransitionMarkers(text: String): String {
        return text.replace(TRANSITION_MARKER_REGEX, "").trim()
    }

    /** Маркер для перехода на следующий этап (для системных промптов). */
    fun nextStageMarker(from: TaskStage): String? {
        val next = defaultNextStage(from) ?: return null
        return transitionMarkerFor(next)
    }

    /** Маркер для перехода на указанный этап. */
    fun transitionMarkerFor(stage: TaskStage): String = "[[TRANSITION:${stage.name}]]"
}
