package ru.sapozhnikov.aiagent.data.repository

import ru.sapozhnikov.aiagent.data.remote.DeepSeekApi
import ru.sapozhnikov.aiagent.data.remote.dto.ChatCompletionRequest
import ru.sapozhnikov.aiagent.data.remote.dto.ChatMessageDto
import ru.sapozhnikov.aiagent.data.remote.dto.ThinkingDto
import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.model.TokenUsage
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import javax.inject.Inject

/** Реализация [AiAgentRepository] через DeepSeek Chat Completions API. */
internal class AiAgentRepositoryImpl @Inject constructor(
    private val api: DeepSeekApi,
) : AiAgentRepository {

    override suspend fun sendMessage(
        context: ApiConversationContext,
        userMessage: String,
    ): Result<AiAgentMessage> {
        return try {
            val requestMessages = buildList {
                add(
                    ChatMessageDto(
                        role = "system",
                        content = CHAT_SYSTEM_PROMPT,
                    ),
                )
                context.summary?.let { summary ->
                    add(
                        ChatMessageDto(
                            role = MessageRole.AI.toApiRole(),
                            content = summary.text,
                        ),
                    )
                }
                addAll(
                    context.messages.map { message ->
                        ChatMessageDto(
                            role = message.role.toApiRole(),
                            content = message.text,
                        )
                    },
                )
                add(
                    ChatMessageDto(
                        role = MessageRole.USER.toApiRole(),
                        content = userMessage,
                    ),
                )
            }
            val request = ChatCompletionRequest(
                model = MODEL,
                messages = requestMessages,
                thinking = ThinkingDto(type = "disabled"),
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

    override suspend fun summarizeMessages(
        messages: List<ChatHistoryMessage>,
        existingSummary: String?,
    ): Result<String> {
        return try {
            val conversationText = messages.joinToString(separator = "\n") { message ->
                val roleLabel = when (message.role) {
                    MessageRole.USER -> "Пользователь"
                    MessageRole.AI -> "Ассистент"
                }
                "$roleLabel: ${message.text}"
            }
            val userContent = buildString {
                if (!existingSummary.isNullOrBlank()) {
                    appendLine("Предыдущее резюме:")
                    appendLine(existingSummary)
                    appendLine()
                    appendLine("Новые сообщения для добавления в резюме:")
                } else {
                    appendLine("Сообщения для резюмирования:")
                }
                append(conversationText)
            }
            val request = ChatCompletionRequest(
                model = MODEL,
                messages = listOf(
                    ChatMessageDto(
                        role = "system",
                        content = SUMMARY_SYSTEM_PROMPT,
                    ),
                    ChatMessageDto(
                        role = "user",
                        content = userContent,
                    ),
                ),
                thinking = ThinkingDto(type = "disabled"),
            )
            val response = api.createChatCompletion(request)
            val content = response.choices.firstOrNull()?.message?.content
            if (content.isNullOrBlank()) {
                Result.failure(IllegalStateException("Пустой ответ при создании резюме"))
            } else {
                Result.success(content.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val MODEL = "deepseek-v4-flash"
        const val CHAT_SYSTEM_PROMPT =
            "Отвечай максимально коротко и чётко, без лишней воды и многословия. " +
                "Давай только суть: факты, выводы и конкретные шаги — насколько это возможно."
        const val SUMMARY_SYSTEM_PROMPT =
            "Сожми переданный тебе диалог до 1-2 предложений. По сути, просто коротко опиши суть того, что в этой беседе обсуждали в этих конкретных сообщениях"
    }
}
