package ru.sapozhnikov.aiagent.data.repository

import ru.sapozhnikov.aiagent.data.remote.DeepSeekApi
import ru.sapozhnikov.aiagent.data.remote.dto.ChatCompletionRequest
import ru.sapozhnikov.aiagent.data.remote.dto.ChatMessageDto
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import javax.inject.Inject

internal class AiAgentRepositoryImpl @Inject constructor(
    private val api: DeepSeekApi,
) : AiAgentRepository {

    override suspend fun sendMessage(userMessage: String): Result<String> {
        return try {
            val request = ChatCompletionRequest(
                model = MODEL,
                messages = listOf(
                    ChatMessageDto(role = "user", content = userMessage),
                ),
            )
            val response = api.createChatCompletion(request)
            val content = response.choices.firstOrNull()?.message?.content
            if (content.isNullOrBlank()) {
                Result.failure(IllegalStateException("Пустой ответ от DeepSeek API"))
            } else {
                Result.success(content)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val MODEL = "deepseek-chat"
    }
}
