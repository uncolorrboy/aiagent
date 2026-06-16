package ru.sapozhnikov.aiagent.domain.model

/**
 * Контекст, подготавливаемый для отправки в LLM API.
 *
 * [facts], [workingMemory], [profileMemory] и [messages] хранятся и обрабатываются раздельно;
 * объединение в единый API-запрос происходит только в [ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository].
 *
 * @property summary сжатое резюме старых сообщений или null
 * @property facts блок ключ-значение важных фактов из диалога или null
 * @property workingMemory выбранная рабочая память для диалога или null
 * @property profileMemory выбранная долговременная память для диалога или null
 * @property messages сообщения, отправляемые в API (краткосрочная память)
 */
internal data class ApiConversationContext(
    val summary: ConversationSummary? = null,
    val facts: Map<String, String>? = null,
    val workingMemory: MemoryInstance? = null,
    val profileMemory: MemoryInstance? = null,
    val messages: List<ChatHistoryMessage>,
)
