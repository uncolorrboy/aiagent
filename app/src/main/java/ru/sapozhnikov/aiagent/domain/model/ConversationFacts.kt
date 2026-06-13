package ru.sapozhnikov.aiagent.domain.model

/**
 * Блок ключ-значение фактов, извлечённых из диалога.
 *
 * @property conversationId идентификатор диалога
 * @property facts карта фактов (цель, ограничения, предпочтения и т.д.)
 * @property updatedAt время последнего обновления (мс)
 */
internal data class ConversationFacts(
    val conversationId: String,
    val facts: Map<String, String>,
    val updatedAt: Long,
)
