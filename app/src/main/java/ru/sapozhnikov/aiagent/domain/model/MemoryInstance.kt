package ru.sapozhnikov.aiagent.domain.model

/**
 * Экземпляр памяти: название и текстовое содержимое.
 *
 * @property id уникальный идентификатор
 * @property name отображаемое название
 * @property text содержимое памяти
 */
internal data class MemoryInstance(
    val id: String,
    val name: String,
    val text: String,
)
