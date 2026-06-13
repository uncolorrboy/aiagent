package ru.sapozhnikov.aiagent.domain.model

/**
 * Контекст, подготавливаемый для отправки в LLM API.
 *
 * [facts] и [messages] хранятся и обрабатываются раздельно;
 * объединение в единый API-запрос происходит только в [ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository].
 *
 * @property summary сжатое резюме старых сообщений или null
 * @property facts блок ключ-значение важных фактов из диалога или null
 * @property messages сообщения, отправляемые в API
 */
internal data class ApiConversationContext(
    val summary: ConversationSummary? = null,
    val facts: Map<String, String>? = null,
    val messages: List<ChatHistoryMessage>,
)
