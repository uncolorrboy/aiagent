package ru.sapozhnikov.aiagent.domain.model

/**
 * Резюме диалога, хранящееся отдельно от обычных сообщений чата.
 *
 * @property conversationId идентификатор диалога
 * @property text текст резюме
 * @property coversUpToMessageId id последнего сообщения из таблицы messages,
 *           которое уже вошло в это резюме
 * @property updatedAt время последнего обновления (мс)
 */
internal data class ConversationSummary(
    val conversationId: String,
    val text: String,
    val coversUpToMessageId: Long,
    val updatedAt: Long,
)
