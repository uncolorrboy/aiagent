package ru.sapozhnikov.aiagent.domain.model

/**
 * Диалог (чат) в списке истории.
 *
 * @property id уникальный идентификатор диалога
 * @property title заголовок, обычно формируется из первого сообщения
 * @property updatedAt время последнего обновления (мс)
 * @property mode режим диалога — обычный чат или задача
 */
internal data class Conversation(
    val id: String,
    val title: String,
    val updatedAt: Long,
    val mode: ConversationMode = ConversationMode.CHAT,
)
