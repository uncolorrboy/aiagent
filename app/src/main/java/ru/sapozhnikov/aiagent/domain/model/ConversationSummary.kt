package ru.sapozhnikov.aiagent.domain.model

/**
 * Резюме диалога, хранящееся отдельно от обычных сообщений чата.
 *
 * @param coversUpToMessageId id последнего сообщения из таблицы messages,
 *        которое уже вошло в это резюме.
 */
internal data class ConversationSummary(
    val conversationId: String,
    val text: String,
    val coversUpToMessageId: Long,
    val updatedAt: Long,
)
