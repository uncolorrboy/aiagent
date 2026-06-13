package ru.sapozhnikov.aiagent.domain.interactor

import android.util.Log
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.ConversationSummary
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import ru.sapozhnikov.aiagent.domain.repository.ChatHistoryRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationSummaryRepository
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Управляет контекстом, который отправляется в LLM API.
 *
 * Обычные сообщения чата и summary — разные сущности:
 * - сообщения живут в таблице messages и отображаются в UI;
 * - summary живёт в таблице conversation_summaries и подставляется в API только на этапе запроса.
 */
internal class ConversationContextInteractor @Inject constructor(
    private val chatHistoryRepository: ChatHistoryRepository,
    private val conversationSummaryRepository: ConversationSummaryRepository,
    private val aiAgentRepository: AiAgentRepository,
    private val settingsRepository: SettingsRepository,
) {

    /**
     * Собирает контекст диалога для отправки в LLM API.
     * При включённом управлении контекстом подставляет резюме и обрезает историю.
     */
    suspend fun getContextForApi(conversationId: String): ApiConversationContext {
        val allMessages = chatHistoryRepository.getMessagesForApi(conversationId)

        if (!settingsRepository.isContextManagementEnabled()) {
            return ApiConversationContext(summary = null, messages = allMessages)
        }

        return buildContextWindow(conversationId, allMessages)
    }

    /**
     * Проверяет, накопилось ли достаточно несжатых сообщений,
     * и при необходимости запрашивает обновление резюме у LLM.
     */
    suspend fun updateSummaryIfNeeded(conversationId: String) {
        if (!settingsRepository.isContextManagementEnabled()) return

        val allMessages = chatHistoryRepository.getMessages(conversationId)
        if (allMessages.size <= RECENT_MESSAGES_COUNT) return

        val messagesBeforeRecent = allMessages.dropLast(RECENT_MESSAGES_COUNT)
        val existingSummary = conversationSummaryRepository.getSummary(conversationId)
        val lastSummarizedId = existingSummary?.coversUpToMessageId ?: 0L
        val unsummarized = messagesBeforeRecent.filter { it.id > lastSummarizedId }

        if (unsummarized.size < SUMMARY_BATCH_SIZE) return

        Log.i(
            TAG,
            buildString {
                appendLine("Начинаем сжатие контекста")
                appendLine("  conversationId: $conversationId")
                appendLine("  сообщений к сжатию: ${unsummarized.size}")
                appendLine("  id сообщений: ${unsummarized.formatMessageIds()}")
                appendLine("  обновление существующего summary: ${existingSummary != null}")
                if (existingSummary != null) {
                    appendLine("  предыдущий coversUpToMessageId: ${existingSummary.coversUpToMessageId}")
                    appendLine("  предыдущий summary:")
                    appendLine(existingSummary.text.prependIndent("    "))
                }
                append("  исходные сообщения:")
                appendLine()
                append(unsummarized.formatForLog().prependIndent("    "))
            },
        )

        aiAgentRepository.summarizeMessages(
            messages = unsummarized,
            existingSummary = existingSummary?.text,
        ).onSuccess { newSummaryText ->
            val savedSummary = ConversationSummary(
                conversationId = conversationId,
                text = newSummaryText,
                coversUpToMessageId = unsummarized.last().id,
                updatedAt = System.currentTimeMillis(),
            )
            conversationSummaryRepository.saveSummary(savedSummary)

            Log.i(
                TAG,
                buildString {
                    appendLine("Сжатие завершено")
                    appendLine("  conversationId: $conversationId")
                    appendLine("  coversUpToMessageId: ${savedSummary.coversUpToMessageId}")
                    appendLine("  длина summary: ${newSummaryText.length} символов")
                    appendLine("  результат summary:")
                    append(newSummaryText.prependIndent("    "))
                },
            )
        }.onFailure { error ->
            Log.w(
                TAG,
                "Сжатие не удалось: conversationId=$conversationId, " +
                    "сообщений=${unsummarized.size}, ids=${unsummarized.formatMessageIds()}",
                error,
            )
        }
    }

    private suspend fun buildContextWindow(
        conversationId: String,
        allMessages: List<ChatHistoryMessage>,
    ): ApiConversationContext {
        if (allMessages.size <= RECENT_MESSAGES_COUNT) {
            return ApiConversationContext(summary = null, messages = allMessages)
        }

        val summary = conversationSummaryRepository.getSummary(conversationId)
        val recentMessages = allMessages.takeLast(RECENT_MESSAGES_COUNT)

        if (summary == null) {
            return ApiConversationContext(summary = null, messages = allMessages)
        }

        val messagesBeforeRecent = allMessages.dropLast(RECENT_MESSAGES_COUNT)
        val gapMessages = messagesBeforeRecent.filter { message ->
            message.id > summary.coversUpToMessageId
        }

        return ApiConversationContext(
            summary = summary,
            messages = gapMessages + recentMessages,
        ).also { context ->
            Log.d(
                TAG,
                buildString {
                    appendLine("Контекст для API собран с summary")
                    appendLine("  conversationId: $conversationId")
                    appendLine("  всего сообщений в чате: ${allMessages.size}")
                    appendLine("  summary coversUpToMessageId: ${summary.coversUpToMessageId}")
                    appendLine("  gap-сообщений: ${gapMessages.size}")
                    appendLine("  recent-сообщений: ${recentMessages.size}")
                    appendLine("  в API уйдёт: 1 summary + ${context.messages.size} сообщений")
                    appendLine("  текст summary:")
                    append(summary.text.prependIndent("    "))
                },
            )
        }
    }

    private fun List<ChatHistoryMessage>.formatMessageIds(): String {
        return joinToString(prefix = "[", postfix = "]") { it.id.toString() }
    }

    private fun List<ChatHistoryMessage>.formatForLog(): String {
        return joinToString(separator = "\n") { message ->
            val roleLabel = when (message.role) {
                MessageRole.USER -> "Пользователь"
                MessageRole.AI -> "Ассистент"
            }
            val preview = message.text
                .replace("\n", " ")
                .take(MESSAGE_PREVIEW_LENGTH)
                .let { text -> if (message.text.length > MESSAGE_PREVIEW_LENGTH) "$text…" else text }
            "#${message.id} $roleLabel: $preview"
        }
    }

    private companion object {
        const val TAG = "ContextCompression"
        const val MESSAGE_PREVIEW_LENGTH = 120
        const val RECENT_MESSAGES_COUNT = 5
        const val SUMMARY_BATCH_SIZE = 5
    }
}
