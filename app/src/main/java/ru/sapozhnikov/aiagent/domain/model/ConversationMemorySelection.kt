package ru.sapozhnikov.aiagent.domain.model

/**
 * Привязка памяти к диалогу (выбирается один раз на диалог).
 *
 * @property conversationId идентификатор диалога
 * @property workingMemoryId выбранный экземпляр рабочей памяти или null
 * @property profileMemoryId выбранный экземпляр долговременной памяти или null
 */
internal data class ConversationMemorySelection(
    val conversationId: String,
    val workingMemoryId: String? = null,
    val profileMemoryId: String? = null,
)
