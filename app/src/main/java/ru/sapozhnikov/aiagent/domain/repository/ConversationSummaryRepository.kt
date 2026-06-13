package ru.sapozhnikov.aiagent.domain.repository

import ru.sapozhnikov.aiagent.domain.model.ConversationSummary

/** Репозиторий для хранения и получения резюме диалогов. */
internal interface ConversationSummaryRepository {

    /** Возвращает резюме диалога или null, если оно ещё не создано. */
    suspend fun getSummary(conversationId: String): ConversationSummary?

    /** Сохраняет или обновляет резюме диалога. */
    suspend fun saveSummary(summary: ConversationSummary)
}
