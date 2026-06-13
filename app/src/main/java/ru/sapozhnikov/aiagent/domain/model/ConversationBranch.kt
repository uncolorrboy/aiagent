package ru.sapozhnikov.aiagent.domain.model

/**
 * Ветка диалога, ответвляющаяся от checkpoint.
 *
 * @property id уникальный идентификатор ветки
 * @property conversationId идентификатор родительского диалога
 * @property name отображаемое имя ветки
 * @property checkpointMessageId id сообщения, от которого начинается ветка
 * @property isActive активна ли ветка в UI
 */
internal data class ConversationBranch(
    val id: String,
    val conversationId: String,
    val name: String,
    val checkpointMessageId: Long,
    val isActive: Boolean,
)
