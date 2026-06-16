package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.ConversationMemorySelection

/** Репозиторий привязки памяти к диалогам. */
internal interface ConversationMemoryRepository {

    fun observeSelection(conversationId: String): Flow<ConversationMemorySelection?>

    suspend fun getSelection(conversationId: String): ConversationMemorySelection?

    suspend fun saveSelection(selection: ConversationMemorySelection)
}
