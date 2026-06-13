package ru.sapozhnikov.aiagent.domain.model

/** Тип содержимого сообщения в истории чата. */
internal enum class MessageKind {
    /** Обычное текстовое сообщение. */
    TEXT,

    /** Сообщение с прикреплённым текстовым файлом. */
    FILE,
}
