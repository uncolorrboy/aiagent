package ru.sapozhnikov.aiagent.data.repository

import ru.sapozhnikov.aiagent.data.remote.DeepSeekApi
import ru.sapozhnikov.aiagent.data.remote.dto.ChatCompletionRequest
import ru.sapozhnikov.aiagent.data.remote.dto.ChatMessageDto
import ru.sapozhnikov.aiagent.data.remote.dto.ThinkingDto
import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.TokenUsage
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import javax.inject.Inject

internal class AiAgentRepositoryImpl @Inject constructor(
    private val api: DeepSeekApi,
) : AiAgentRepository {

    override suspend fun sendMessage(messages: List<ChatHistoryMessage>): Result<AiAgentMessage> {
        return try {
            val request = ChatCompletionRequest(
                model = MODEL,
                messages = messages.map { message ->
                    ChatMessageDto(
                        role = message.role.toApiRole(),
                        content = message.text,
                    )
                },
                thinking = ThinkingDto(type = "disabled")
            )
            val response = api.createChatCompletion(request)
            val content = response.choices.firstOrNull()?.message?.content
            val usage = response.usage
            if (content.isNullOrBlank()) {
                Result.failure(IllegalStateException("Пустой ответ от DeepSeek API"))
            } else if (usage == null) {
                Result.failure(IllegalStateException("Ответ API не содержит данных о токенах"))
            } else {
                Result.success(
                    AiAgentMessage(
                        text = content,
                        usage = TokenUsage(
                            promptTokens = usage.promptTokens,
                            completionTokens = usage.completionTokens,
                            totalTokens = usage.totalTokens,
                            promptCacheHitTokens = usage.promptCacheHitTokens,
                            promptCacheMissTokens = usage.promptCacheMissTokens,
                        ),
                    ),
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val MODEL = "deepseek-v4-flash"
    }
}
