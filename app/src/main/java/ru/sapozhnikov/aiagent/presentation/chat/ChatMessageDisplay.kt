package ru.sapozhnikov.aiagent.presentation.chat

/** Максимальная длина пользовательского сообщения, отображаемого в UI. */
internal const val USER_MESSAGE_DISPLAY_MAX_LENGTH = 4096

/** Обрезает длинный текст пользовательского сообщения для отображения в пузыре чата. */
internal fun truncateUserMessageForDisplay(text: String): String {
    if (text.length <= USER_MESSAGE_DISPLAY_MAX_LENGTH) return text
    return text.take(USER_MESSAGE_DISPLAY_MAX_LENGTH) + "…"
}
