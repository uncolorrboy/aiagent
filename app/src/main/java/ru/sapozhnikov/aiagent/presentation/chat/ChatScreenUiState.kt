package ru.sapozhnikov.aiagent.presentation.chat

internal data class ChatScreenUiState(
    val isLoading: Boolean = false,
    val items: List<ChatMessage> = listOf(),
    val sendButtonState: SendButtonState = SendButtonState.DISABLED,
)

internal data class ChatMessage(
    val text: String,
    val time: String,
    val messageOwner: MessageOwner,
)

internal enum class MessageOwner {
    AI, USER
}

internal enum class SendButtonState {
    DISABLED, DEFAULT, IN_PROCESS,
}