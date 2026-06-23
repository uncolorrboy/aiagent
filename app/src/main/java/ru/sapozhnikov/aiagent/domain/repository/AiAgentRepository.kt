package ru.sapozhnikov.aiagent.domain.repository

import ru.sapozhnikov.aiagent.data.remote.dto.ToolDto
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
     * Отправляет сообщение с поддержкой tool calling и выполняет инструменты через [toolExecutor].
     */
    suspend fun sendMessageWithTools(
        context: ApiConversationContext,
        userMessage: String,
        tools: List<ToolDto>,
        toolExecutor: suspend (name: String, argumentsJson: String) -> Result<String>,
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

    /**
     * Извлекает и обновляет блок фактов (ключ-значение) из диалога.
     *
     * @param messages текущая история сообщений
     * @param newUserMessage последнее сообщение пользователя
     * @param existingFacts уже сохранённые факты
     */
    suspend fun extractFacts(
        messages: List<ChatHistoryMessage>,
        newUserMessage: String,
        existingFacts: Map<String, String>?,
    ): Result<Map<String, String>>
}
