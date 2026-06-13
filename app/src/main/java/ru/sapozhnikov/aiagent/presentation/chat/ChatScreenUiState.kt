package ru.sapozhnikov.aiagent.presentation.chat

/**
 * Состояние UI экрана чата.
 *
 * @property isLoading идёт ли сейчас запрос к LLM
 * @property items сообщения в ленте чата
 * @property sendButtonState состояние кнопки отправки
 * @property totalTokenCount суммарное число токенов последнего запроса
 * @property chatPrice расчётная стоимость диалога в USD
 */
internal data class ChatScreenUiState(
    val isLoading: Boolean = false,
    val items: List<ChatMessage> = listOf(),
    val sendButtonState: SendButtonState = SendButtonState.DISABLED,
    val totalTokenCount: Int = 0,
    val chatPrice: Double = 0.0,
)

/**
 * UI-модель сообщения в ленте чата.
 *
 * @property text текст сообщения или превью содержимого
 * @property time отформатированное время отправки
 * @property messageOwner отправитель — пользователь или ассистент
 * @property tokenCount число токенов (для пользовательских сообщений)
 * @property kind тип содержимого — текст или файл
 * @property attachmentFileName имя прикреплённого файла
 */
internal data class ChatMessage(
    val text: String,
    val time: String,
    val messageOwner: MessageOwner,
    val tokenCount: Int? = null,
    val kind: MessageKind = MessageKind.TEXT,
    val attachmentFileName: String? = null,
)

/** Тип содержимого сообщения в UI. */
internal enum class MessageKind {
    /** Текстовое сообщение. */
    TEXT,

    /** Сообщение с прикреплённым файлом. */
    FILE,
}

/** Отправитель сообщения в UI. */
internal enum class MessageOwner {
    /** Ответ LLM-ассистента. */
    AI,

    /** Сообщение пользователя. */
    USER,
}

/** Состояние кнопки отправки сообщения. */
internal enum class SendButtonState {
    /** Кнопка неактивна (пустой ввод). */
    DISABLED,

    /** Готова к отправке. */
    DEFAULT,

    /** Запрос к LLM выполняется. */
    IN_PROCESS,
}
