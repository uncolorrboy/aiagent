package ru.sapozhnikov.aiagent.domain.model

/**
 * Сообщение в истории чата.
 *
 * @property id идентификатор записи в БД
 * @property conversationId идентификатор диалога
 * @property text текст сообщения (для файловых — может быть пустым в UI)
 * @property role роль отправителя
 * @property timestamp время создания (мс)
 * @property cacheHitTokens токены промпта из кэша (только для пользовательских сообщений)
 * @property tokenCount число токенов сообщения
 * @property kind тип сообщения — текст или файл
 * @property attachmentUri URI сохранённого файла-вложения
 * @property attachmentFileName отображаемое имя прикреплённого файла
 * @property branchId идентификатор ветки (null — общая часть до checkpoint)
 * @property taskStage этап задачи (null — обычный чат или legacy-сообщение)
 */
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
    val branchId: String? = null,
    val taskStage: TaskStage? = null,
)

/** Роль участника диалога при обмене сообщениями с LLM API. */
internal enum class MessageRole {
    /** Сообщение пользователя. */
    USER,

    /** Ответ ассистента (модели). */
    AI;

    /** Преобразует роль в строковое значение для API (user / assistant). */
    fun toApiRole(): String = when (this) {
        USER -> "user"
        AI -> "assistant"
    }
}
