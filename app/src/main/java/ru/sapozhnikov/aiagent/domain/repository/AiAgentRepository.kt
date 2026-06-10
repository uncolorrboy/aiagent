package ru.sapozhnikov.aiagent.domain.repository

import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage

internal interface AiAgentRepository {

    suspend fun sendMessage(messages: List<ChatHistoryMessage>): Result<AiAgentMessage>
}
