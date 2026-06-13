package ru.sapozhnikov.aiagent.domain.repository

import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage

/** Репозиторий для взаимодействия с LLM API (DeepSeek). */
internal interface AiAgentRepository {

    /**
     * Отправляет сообщение пользователя в LLM с учётом контекста диалога.
     *
     * @param context подготовленный контекст (резюме + сообщения)
     * @param userMessage новое сообщение пользователя
     */
    suspend fun sendMessage(
        context: ApiConversationContext,
        userMessage: String,
    ): Result<AiAgentMessage>

    /**
     * Сжимает набор сообщений в краткое резюме.
     *
     * @param messages сообщения для резюмирования
     * @param existingSummary предыдущее резюме для инкрементального обновления
     */
    suspend fun summarizeMessages(
        messages: List<ChatHistoryMessage>,
        existingSummary: String?,
    ): Result<String>
}
