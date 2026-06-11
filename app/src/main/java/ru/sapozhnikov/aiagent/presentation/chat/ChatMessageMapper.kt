package ru.sapozhnikov.aiagent.presentation.chat

import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun ChatHistoryMessage.toUiModel(): ChatMessage {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    return ChatMessage(
        text = text,
        time = time,
        messageOwner = when (role) {
            MessageRole.USER -> MessageOwner.USER
            MessageRole.AI -> MessageOwner.AI
        },
        tokenCount = tokenCount,
        kind = when (kind) {
            ru.sapozhnikov.aiagent.domain.model.MessageKind.TEXT -> MessageKind.TEXT
            ru.sapozhnikov.aiagent.domain.model.MessageKind.FILE -> MessageKind.FILE
        },
        attachmentFileName = attachmentFileName,
    )
}
