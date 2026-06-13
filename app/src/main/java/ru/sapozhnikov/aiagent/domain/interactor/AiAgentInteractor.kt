package ru.sapozhnikov.aiagent.domain.interactor

import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import javax.inject.Inject

internal class AiAgentInteractor @Inject constructor(
    private val repository: AiAgentRepository,
) {

    suspend fun sendMessage(
        context: ApiConversationContext,
        userMessage: String,
    ): Result<AiAgentMessage> {
        if (userMessage.isBlank()) {
            return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
        }
        return repository.sendMessage(context, userMessage.trim())
    }
}
