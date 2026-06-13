package ru.sapozhnikov.aiagent.domain.model

/**
 * Контекст, подготавливаемый для отправки в LLM API.
 *
 * [summary] и [messages] хранятся и обрабатываются раздельно;
 * объединение в единый API-запрос происходит только в [ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository].
 */
internal data class ApiConversationContext(
    val summary: ConversationSummary?,
    val messages: List<ChatHistoryMessage>,
)
