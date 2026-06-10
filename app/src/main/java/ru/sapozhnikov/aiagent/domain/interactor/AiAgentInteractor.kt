package ru.sapozhnikov.aiagent.domain.interactor

import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import javax.inject.Inject

internal class AiAgentInteractor @Inject constructor(
    private val repository: AiAgentRepository,
) {

    suspend fun sendMessage(
        history: List<ChatHistoryMessage>,
        userMessage: String,
    ): Result<String> {
        if (userMessage.isBlank()) {
            return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
        }
        val pendingUserMessage = ChatHistoryMessage(
            id = 0,
            conversationId = "",
            text = userMessage.trim(),
            role = MessageRole.USER,
            timestamp = System.currentTimeMillis(),
        )
        return repository.sendMessage(history + pendingUserMessage)
    }
}
