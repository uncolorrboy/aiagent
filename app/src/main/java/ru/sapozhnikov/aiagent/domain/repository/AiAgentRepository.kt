package ru.sapozhnikov.aiagent.domain.repository

internal interface AiAgentRepository {

    suspend fun sendMessage(userMessage: String): Result<String>
}
