package ru.sapozhnikov.aiagent.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.sapozhnikov.aiagent.data.remote.DeepSeekApi
import ru.sapozhnikov.aiagent.data.remote.dto.ChatCompletionRequest
import ru.sapozhnikov.aiagent.data.remote.dto.ChatMessageDto
import ru.sapozhnikov.aiagent.data.remote.dto.ThinkingDto
import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.model.TaskStagePrompts
import ru.sapozhnikov.aiagent.domain.model.TokenUsage
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import javax.inject.Inject

/** Реализация [AiAgentRepository] через DeepSeek Chat Completions API. */
internal class AiAgentRepositoryImpl @Inject constructor(
    private val api: DeepSeekApi,
) : AiAgentRepository {

    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, String>>() {}.type

    override suspend fun sendMessage(
        context: ApiConversationContext,
        userMessage: String,
    ): Result<AiAgentMessage> {
        return try {
            val requestMessages = buildList {
                context.taskSystemPrompt?.let { taskPrompt ->
                    val artifactsBlock = TaskStagePrompts.formatArtifactsBlock(context.taskArtifacts)
                    val fullPrompt = if (artifactsBlock.isNotBlank()) {
                        "$taskPrompt\n\n$artifactsBlock"
                    } else {
                        taskPrompt
                    }
                    add(
                        ChatMessageDto(
                            role = "system",
                            content = fullPrompt,
                        ),
                    )
                }
                context.profileMemory?.let { profile ->
                    add(
                        ChatMessageDto(
                            role = "system",
                            content = formatProfileMemoryBlock(profile),
                        ),
                    )
                }
                context.workingMemory?.let { working ->
                    add(
                        ChatMessageDto(
                            role = "system",
                            content = formatWorkingMemoryBlock(working),
                        ),
                    )
                }
                context.facts?.takeIf { it.isNotEmpty() }?.let { facts ->
                    add(
                        ChatMessageDto(
                            role = "system",
                            content = formatFactsBlock(facts),
                        ),
                    )
                }
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

    override suspend fun extractFacts(
        messages: List<ChatHistoryMessage>,
        newUserMessage: String,
        existingFacts: Map<String, String>?,
    ): Result<Map<String, String>> {
        return try {
            val conversationText = messages.joinToString(separator = "\n") { message ->
                val roleLabel = when (message.role) {
                    MessageRole.USER -> "Пользователь"
                    MessageRole.AI -> "Ассистент"
                }
                "$roleLabel: ${message.text}"
            }
            val userContent = buildString {
                if (!existingFacts.isNullOrEmpty()) {
                    appendLine("Текущие факты:")
                    existingFacts.forEach { (key, value) ->
                        appendLine("$key: $value")
                    }
                    appendLine()
                }
                appendLine("История диалога:")
                appendLine(conversationText)
                appendLine()
                appendLine("Новое сообщение пользователя:")
                append(newUserMessage)
            }
            val request = ChatCompletionRequest(
                model = MODEL,
                messages = listOf(
                    ChatMessageDto(
                        role = "system",
                        content = FACTS_SYSTEM_PROMPT,
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
                Result.failure(IllegalStateException("Пустой ответ при извлечении фактов"))
            } else {
                Result.success(parseFactsResponse(content.trim(), existingFacts))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseFactsResponse(
        content: String,
        existingFacts: Map<String, String>?,
    ): Map<String, String> {
        val jsonStart = content.indexOf('{')
        val jsonEnd = content.lastIndexOf('}')
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            val json = content.substring(jsonStart, jsonEnd + 1)
            return runCatching {
                gson.fromJson<Map<String, String>>(json, mapType) ?: emptyMap()
            }.getOrElse { existingFacts.orEmpty() }
        }
        return existingFacts.orEmpty()
    }

    private fun formatFactsBlock(facts: Map<String, String>): String {
        return buildString {
            appendLine("Важные факты из диалога:")
            facts.forEach { (key, value) ->
                appendLine("- $key: $value")
            }
        }.trim()
    }

    private fun formatWorkingMemoryBlock(memory: MemoryInstance): String {
        return buildString {
            appendLine("Рабочая память (${memory.name}):")
            append(memory.text)
        }.trim()
    }

    private fun formatProfileMemoryBlock(memory: MemoryInstance): String {
        return buildString {
            appendLine("Долговременная память / профиль (${memory.name}):")
            append(memory.text)
        }.trim()
    }

    private companion object {
        const val MODEL = "deepseek-v4-flash"
        const val SUMMARY_SYSTEM_PROMPT =
            "Сожми переданный тебе диалог до 1-2 предложений. По сути, просто коротко опиши суть того, что в этой беседе обсуждали в этих конкретных сообщениях"
        const val FACTS_SYSTEM_PROMPT =
            "Извлеки важные факты из диалога: цель, ограничения, предпочтения, решения, договорённости. " +
                "Верни ТОЛЬКО JSON-объект вида {\"ключ\": \"значение\"}. " +
                "Обнови существующие факты с учётом нового сообщения. " +
                "Используй короткие русские ключи. Не добавляй пояснений вне JSON."
    }
}
