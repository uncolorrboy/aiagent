package ru.sapozhnikov.aiagent.presentation.chatlist

internal data class ChatListUiState(
    val conversations: List<ConversationItem> = emptyList(),
)

internal data class ConversationItem(
    val id: String,
    val title: String,
    val updatedAt: String,
)
