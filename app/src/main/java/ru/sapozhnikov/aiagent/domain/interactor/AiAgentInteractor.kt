package ru.sapozhnikov.aiagent.domain.interactor

import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import javax.inject.Inject

internal class AiAgentInteractor @Inject constructor(
    private val repository: AiAgentRepository,
) {

    suspend fun sendMessage(userMessage: String): Result<String> {
        if (userMessage.isBlank()) {
            return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
        }
        return repository.sendMessage(userMessage.trim())
    }
}
