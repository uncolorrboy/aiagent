package ru.sapozhnikov.aiagent.domain.repository

import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage

internal interface AiAgentRepository {

    suspend fun sendMessage(
        context: ApiConversationContext,
        userMessage: String,
    ): Result<AiAgentMessage>

    suspend fun summarizeMessages(
        messages: List<ChatHistoryMessage>,
        existingSummary: String?,
    ): Result<String>
}
