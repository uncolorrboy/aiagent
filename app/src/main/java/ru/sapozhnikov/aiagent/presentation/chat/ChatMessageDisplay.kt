package ru.sapozhnikov.aiagent.presentation.chat

internal const val USER_MESSAGE_DISPLAY_MAX_LENGTH = 4096

internal fun truncateUserMessageForDisplay(text: String): String {
    if (text.length <= USER_MESSAGE_DISPLAY_MAX_LENGTH) return text
    return text.take(USER_MESSAGE_DISPLAY_MAX_LENGTH) + "…"
}
