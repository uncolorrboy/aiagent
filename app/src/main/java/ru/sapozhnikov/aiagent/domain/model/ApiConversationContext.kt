package ru.sapozhnikov.aiagent.domain.model

/**
 * Контекст, подготавливаемый для отправки в LLM API.
 *
 * [summary] и [messages] хранятся и обрабатываются раздельно;
 * объединение в единый API-запрос происходит только в [ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository].
 *
 * @property summary сжатое резюме старых сообщений или null
 * @property messages сообщения, отправляемые в API вместе с резюме
 */
internal data class ApiConversationContext(
    val summary: ConversationSummary?,
    val messages: List<ChatHistoryMessage>,
)
