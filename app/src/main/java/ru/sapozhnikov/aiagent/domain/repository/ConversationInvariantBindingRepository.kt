package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow

/** Репозиторий привязки инвариантов к диалогам. */
internal interface ConversationInvariantBindingRepository {

    fun observeInvariantIds(conversationId: String): Flow<List<String>>

    suspend fun getInvariantIds(conversationId: String): List<String>

    suspend fun saveInvariantIds(conversationId: String, invariantIds: List<String>)

    suspend fun deleteByConversationId(conversationId: String)
}
