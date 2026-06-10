package ru.sapozhnikov.aiagent.domain.model

internal data class ChatHistoryMessage(
    val id: Long,
    val conversationId: String,
    val text: String,
    val role: MessageRole,
    val timestamp: Long,
)

internal enum class MessageRole {
    USER, AI;

    fun toApiRole(): String = when (this) {
        USER -> "user"
        AI -> "assistant"
    }
}
