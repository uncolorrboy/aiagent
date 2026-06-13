package ru.sapozhnikov.aiagent.domain.repository

import ru.sapozhnikov.aiagent.domain.model.ConversationSummary

internal interface ConversationSummaryRepository {

    suspend fun getSummary(conversationId: String): ConversationSummary?

    suspend fun saveSummary(summary: ConversationSummary)
}
