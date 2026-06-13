package ru.sapozhnikov.aiagent.domain.repository

import ru.sapozhnikov.aiagent.domain.model.ConversationFacts

/** Репозиторий для блока фактов диалога. */
internal interface ConversationFactsRepository {

    /** Возвращает факты диалога или null. */
    suspend fun getFacts(conversationId: String): ConversationFacts?

    /** Сохраняет факты диалога. */
    suspend fun saveFacts(facts: ConversationFacts)
}
