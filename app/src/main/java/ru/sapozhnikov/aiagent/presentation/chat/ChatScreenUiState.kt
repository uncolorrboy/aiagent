package ru.sapozhnikov.aiagent.presentation.chat

internal data class ChatScreenUiState(
    val isLoading: Boolean = false,
    val items: List<ChatMessage> = listOf(),
    val sendButtonState: SendButtonState = SendButtonState.DISABLED,
    val totalTokenCount: Int = 0,
    val chatPrice: Double = 0.0,
)

internal data class ChatMessage(
    val text: String,
    val time: String,
    val messageOwner: MessageOwner,
    val tokenCount: Int? = null,
    val kind: MessageKind = MessageKind.TEXT,
    val attachmentFileName: String? = null,
)

internal enum class MessageKind {
    TEXT,
    FILE,
}

internal enum class MessageOwner {
    AI, USER
}

internal enum class SendButtonState {
    DISABLED, DEFAULT, IN_PROCESS,
}