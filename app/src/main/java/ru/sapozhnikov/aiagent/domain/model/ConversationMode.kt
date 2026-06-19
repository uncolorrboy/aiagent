package ru.sapozhnikov.aiagent.domain.model

/** Режим диалога. */
internal enum class ConversationMode {
    /** Обычный свободный чат. */
    CHAT,

    /** Задача с этапами (конечный автомат). */
    TASK,
}
