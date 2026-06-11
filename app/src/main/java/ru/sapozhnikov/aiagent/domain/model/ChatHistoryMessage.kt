package ru.sapozhnikov.aiagent.domain.model

internal data class ChatHistoryMessage(
    val id: Long,
    val conversationId: String,
    val text: String,
    val role: MessageRole,
    val timestamp: Long,
    val cacheHitTokens: Int? = null,
    val tokenCount: Int? = null,
    val kind: MessageKind = MessageKind.TEXT,
    val attachmentUri: String? = null,
    val attachmentFileName: String? = null,
)

internal enum class MessageRole {
    USER, AI;

    fun toApiRole(): String = when (this) {
        USER -> "user"
        AI -> "assistant"
    }
}
