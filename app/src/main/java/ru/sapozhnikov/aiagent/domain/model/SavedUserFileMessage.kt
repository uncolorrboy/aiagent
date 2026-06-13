package ru.sapozhnikov.aiagent.domain.model

/**
 * Результат сохранения пользовательского файлового сообщения.
 *
 * @property content текстовое содержимое файла, отправляемое в LLM API
 * @property fileName отображаемое имя файла
 */
internal data class SavedUserFileMessage(
    val content: String,
    val fileName: String,
)
