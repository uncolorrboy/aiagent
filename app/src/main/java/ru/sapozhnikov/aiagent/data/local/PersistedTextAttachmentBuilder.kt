package ru.sapozhnikov.aiagent.data.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.sapozhnikov.aiagent.extensions.buildAttachmentFile
import ru.sapozhnikov.aiagent.extensions.isSupportedTextFile
import ru.sapozhnikov.aiagent.extensions.readTextContent
import ru.sapozhnikov.aiagent.extensions.resolveDisplayName
import ru.sapozhnikov.aiagent.extensions.toStoredFile
import ru.sapozhnikov.aiagent.extensions.toStoredUriString
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Результат сохранения текстового файла-вложения на диск приложения.
 *
 * @property fileName отображаемое имя файла
 * @property content текстовое содержимое
 * @property storedUri URI сохранённого файла для записи в БД
 */
internal data class PersistedTextAttachment(
    val fileName: String,
    val content: String,
    val storedUri: String,
)

/**
 * Сохраняет текстовые файлы-вложения в локальное хранилище приложения
 * и предоставляет доступ к их содержимому.
 */
@Singleton
internal class PersistedTextAttachmentBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Копирует текстовый файл из [sourceUri] во внутреннее хранилище
     * и возвращает метаданные для сохранения в БД.
     *
     * @throws IllegalArgumentException если формат не поддерживается или файл пуст
     */
    suspend fun build(
        conversationId: String,
        sourceUri: Uri,
    ): PersistedTextAttachment = withContext(Dispatchers.IO) {
        require(sourceUri.isSupportedTextFile(context)) {
            "Поддерживаются только текстовые файлы .txt и .md"
        }

        val fileName = sourceUri.resolveDisplayName(context)
        val content = sourceUri.readTextContent(context)
        require(content.isNotBlank()) { "Файл пуст" }

        val destinationFile = context.filesDir
            .let { filesDir -> buildAttachmentFile(conversationId, fileName).let { relative ->
                File(filesDir, relative.path)
            } }
        destinationFile.parentFile?.mkdirs()

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Не удалось сохранить файл")

        PersistedTextAttachment(
            fileName = fileName,
            content = content,
            storedUri = destinationFile.toStoredUriString(),
        )
    }

    /** Читает текстовое содержимое ранее сохранённого файла-вложения. */
    suspend fun readStoredContent(storedUri: String): String = withContext(Dispatchers.IO) {
        val file = storedUri.toStoredFile()
        if (!file.exists()) {
            error("Файл сообщения не найден")
        }
        file.readText()
    }

    /** Удаляет все файлы-вложения указанного диалога. */
    suspend fun deleteConversationAttachments(conversationId: String) = withContext(Dispatchers.IO) {
        File(context.filesDir, "message_attachments/$conversationId").deleteRecursively()
    }
}
