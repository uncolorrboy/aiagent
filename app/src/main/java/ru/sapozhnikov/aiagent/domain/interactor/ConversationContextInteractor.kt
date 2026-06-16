package ru.sapozhnikov.aiagent.domain.interactor

import android.util.Log
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.ConversationFacts
import ru.sapozhnikov.aiagent.domain.model.ConversationSummary
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import ru.sapozhnikov.aiagent.domain.repository.ChatHistoryRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationBranchRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationFactsRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationMemoryRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationSummaryRepository
import ru.sapozhnikov.aiagent.domain.repository.ProfileMemoryRepository
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import ru.sapozhnikov.aiagent.domain.repository.WorkingMemoryRepository
import javax.inject.Inject

/**
 * Управляет контекстом, который отправляется в LLM API.
 *
 * Поддерживает пять режимов:
 * - [ContextManagementStrategy.DEFAULT] — полная история;
 * - [ContextManagementStrategy.SUMMARY_COMPRESSION] — резюме + последние 5 сообщений;
 * - [ContextManagementStrategy.SLIDING_WINDOW] — последние 10 сообщений;
 * - [ContextManagementStrategy.STICKY_FACTS] — facts + последние 10 сообщений;
 * - [ContextManagementStrategy.BRANCHING] — общая часть + активная ветка.
 */
internal class ConversationContextInteractor @Inject constructor(
    private val chatHistoryRepository: ChatHistoryRepository,
    private val conversationSummaryRepository: ConversationSummaryRepository,
    private val conversationFactsRepository: ConversationFactsRepository,
    private val conversationBranchRepository: ConversationBranchRepository,
    private val conversationMemoryRepository: ConversationMemoryRepository,
    private val workingMemoryRepository: WorkingMemoryRepository,
    private val profileMemoryRepository: ProfileMemoryRepository,
    private val aiAgentRepository: AiAgentRepository,
    private val settingsRepository: SettingsRepository,
) {

    /**
     * Собирает контекст диалога для отправки в LLM API
     * с учётом выбранной стратегии управления контекстом.
     */
    suspend fun getContextForApi(conversationId: String): ApiConversationContext {
        val strategy = settingsRepository.getContextManagementStrategy()
        val allMessages = loadMessages(conversationId, strategy)
        val memorySelection = conversationMemoryRepository.getSelection(conversationId)
        val workingMemory = memorySelection?.workingMemoryId?.let { workingMemoryRepository.getById(it) }
        val profileMemory = memorySelection?.profileMemoryId?.let { profileMemoryRepository.getById(it) }

        val context = when (strategy) {
            ContextManagementStrategy.DEFAULT -> {
                ApiConversationContext(messages = allMessages)
            }

            ContextManagementStrategy.SUMMARY_COMPRESSION -> {
                buildSummaryContextWindow(conversationId, allMessages)
            }

            ContextManagementStrategy.SLIDING_WINDOW -> {
                ApiConversationContext(messages = allMessages.takeLast(SLIDING_WINDOW_COUNT))
            }

            ContextManagementStrategy.STICKY_FACTS -> {
                val facts = conversationFactsRepository.getFacts(conversationId)?.facts
                ApiConversationContext(
                    facts = facts?.takeIf { it.isNotEmpty() },
                    messages = allMessages.takeLast(SLIDING_WINDOW_COUNT),
                )
            }

            ContextManagementStrategy.BRANCHING -> {
                ApiConversationContext(messages = allMessages)
            }
        }

        return context.copy(
            workingMemory = workingMemory,
            profileMemory = profileMemory,
        ).also { enrichedContext ->
            Log.d(
                TAG,
                buildString {
                    append("Контекст для API: strategy=$strategy")
                    append(", messages=${enrichedContext.messages.size}")
                    append(", summary=${enrichedContext.summary != null}")
                    append(", facts=${enrichedContext.facts?.size ?: 0}")
                    append(", workingMemory=${enrichedContext.workingMemory?.name}")
                    append(", profileMemory=${enrichedContext.profileMemory?.name}")
                },
            )
        }
    }

    /**
     * Проверяет, накопилось ли достаточно несжатых сообщений,
     * и при необходимости запрашивает обновление резюме у LLM.
     */
    suspend fun updateSummaryIfNeeded(conversationId: String) {
        if (settingsRepository.getContextManagementStrategy() != ContextManagementStrategy.SUMMARY_COMPRESSION) {
            return
        }

        val allMessages = chatHistoryRepository.getMessages(conversationId)
        if (allMessages.size <= SUMMARY_RECENT_MESSAGES_COUNT) return

        val messagesBeforeRecent = allMessages.dropLast(SUMMARY_RECENT_MESSAGES_COUNT)
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
                "Сжатие завершено: conversationId=$conversationId, " +
                    "coversUpToMessageId=${savedSummary.coversUpToMessageId}",
            )
        }.onFailure { error ->
            Log.w(
                TAG,
                "Сжатие не удалось: conversationId=$conversationId, " +
                    "сообщений=${unsummarized.size}",
                error,
            )
        }
    }

    /**
     * Обновляет блок фактов после сообщения пользователя
     * (только для стратегии [ContextManagementStrategy.STICKY_FACTS]).
     */
    suspend fun updateFactsIfNeeded(conversationId: String, userMessage: String) {
        if (settingsRepository.getContextManagementStrategy() != ContextManagementStrategy.STICKY_FACTS) {
            return
        }

        val allMessages = loadMessages(conversationId, ContextManagementStrategy.DEFAULT)
        val existingFacts = conversationFactsRepository.getFacts(conversationId)?.facts

        aiAgentRepository.extractFacts(
            messages = allMessages,
            newUserMessage = userMessage,
            existingFacts = existingFacts,
        ).onSuccess { updatedFacts ->
            conversationFactsRepository.saveFacts(
                ConversationFacts(
                    conversationId = conversationId,
                    facts = updatedFacts,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            Log.i(TAG, "Facts обновлены: conversationId=$conversationId, count=${updatedFacts.size}")
        }.onFailure { error ->
            Log.w(TAG, "Не удалось обновить facts: conversationId=$conversationId", error)
        }
    }

    private suspend fun buildSummaryContextWindow(
        conversationId: String,
        allMessages: List<ChatHistoryMessage>,
    ): ApiConversationContext {
        if (allMessages.size <= SUMMARY_RECENT_MESSAGES_COUNT) {
            return ApiConversationContext(messages = allMessages)
        }

        val summary = conversationSummaryRepository.getSummary(conversationId)
        val recentMessages = allMessages.takeLast(SUMMARY_RECENT_MESSAGES_COUNT)

        if (summary == null) {
            return ApiConversationContext(messages = allMessages)
        }

        val messagesBeforeRecent = allMessages.dropLast(SUMMARY_RECENT_MESSAGES_COUNT)
        val gapMessages = messagesBeforeRecent.filter { message ->
            message.id > summary.coversUpToMessageId
        }

        return ApiConversationContext(
            summary = summary,
            messages = gapMessages + recentMessages,
        )
    }

    private suspend fun loadMessages(
        conversationId: String,
        strategy: ContextManagementStrategy,
    ): List<ChatHistoryMessage> {
        return if (strategy == ContextManagementStrategy.BRANCHING) {
            val activeBranch = conversationBranchRepository.getActiveBranch(conversationId)
            if (activeBranch != null) {
                chatHistoryRepository.getMessagesForApi(conversationId, activeBranch.id)
            } else {
                chatHistoryRepository.getMessagesForApi(conversationId)
            }
        } else {
            chatHistoryRepository.getMessagesForApi(conversationId)
        }
    }

    private fun List<ChatHistoryMessage>.formatMessageIds(): String {
        return joinToString(prefix = "[", postfix = "]") { it.id.toString() }
    }

    private companion object {
        const val TAG = "ContextManagement"
        const val SLIDING_WINDOW_COUNT = 10
        const val SUMMARY_RECENT_MESSAGES_COUNT = 5
        const val SUMMARY_BATCH_SIZE = 5
    }
}
