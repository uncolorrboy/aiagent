package ru.sapozhnikov.aiagent.domain.model

/**
 * Ответ LLM-ассистента на сообщение пользователя.
 *
 * @property text текст ответа модели
 * @property usage статистика использования токенов для данного запроса
 */
internal data class AiAgentMessage(
    val text: String,
    val usage: TokenUsage,
)
