package ru.sapozhnikov.aiagent.domain.model

/**
 * Инвариант ассистента — обязательное правило, которое нельзя нарушать.
 *
 * Хранится отдельно от истории диалога и привязывается к чату явно.
 *
 * @property id уникальный идентификатор
 * @property name краткое название
 * @property text полное описание правила
 */
internal data class AssistantInvariant(
    val id: String,
    val name: String,
    val text: String,
)
