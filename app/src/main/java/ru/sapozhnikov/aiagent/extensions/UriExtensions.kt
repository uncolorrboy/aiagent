package ru.sapozhnikov.aiagent.extensions

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.util.UUID

/**
 * Читает текстовое содержимое файла по [Uri] в фоновом потоке.
 *
 * @throws IllegalStateException если поток не удалось открыть
 */
internal suspend fun Uri.readTextContent(context: Context): String = withContext(Dispatchers.IO) {
    context.contentResolver.openInputStream(this@readTextContent)?.use { input ->
        input.bufferedReader().readText()
    } ?: error("Не удалось прочитать файл")
}

/**
 * Определяет отображаемое имя файла по [Uri] через ContentResolver или путь.
 */
internal fun Uri.resolveDisplayName(context: Context): String {
    if (scheme == "file") {
        return lastPathSegment?.substringAfterLast('/') ?: "file.txt"
    }
    context.contentResolver.query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                return cursor.getString(nameIndex)
            }
        }
    return lastPathSegment?.substringAfterLast('/') ?: "file.txt"
}

/**
 * Проверяет, является ли имя файла поддерживаемым текстовым форматом (.txt, .md).
 */
internal fun String.isSupportedTextFileName(): Boolean {
    val extension = substringAfterLast('.', "").lowercase()
    return extension in SUPPORTED_TEXT_EXTENSIONS
}

/**
 * Проверяет, является ли файл по [Uri] поддерживаемым текстовым форматом
 * по расширению имени или MIME-типу.
 */
internal fun Uri.isSupportedTextFile(context: Context): Boolean {
    val fileName = resolveDisplayName(context)
    if (fileName.isSupportedTextFileName()) return true
    val mimeType = context.contentResolver.getType(this)
        ?: MimeTypeMap.getFileExtensionFromUrl(toString())?.let { extension ->
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        }
    return mimeType in SUPPORTED_TEXT_MIME_TYPES
}

private val SUPPORTED_TEXT_EXTENSIONS = setOf("txt", "md", "markdown")
private val SUPPORTED_TEXT_MIME_TYPES = setOf(
    "text/plain",
    "text/markdown",
    "text/x-markdown",
)

/**
 * Возвращает размер строки в байтах при кодировке UTF-8.
 */
internal fun String.utf8ByteSize(): Int = toByteArray(Charsets.UTF_8).size

/**
 * Преобразует [File] в строковое URI-представление для хранения в БД.
 */
internal fun File.toStoredUriString(): String = toURI().toString()

/**
 * Восстанавливает [File] из строкового URI-представления.
 */
internal fun String.toStoredFile(): File = File(URI.create(this))

/**
 * Формирует путь к файлу вложения внутри директории приложения.
 *
 * @param conversationId идентификатор диалога
 * @param originalFileName исходное имя файла (используется для определения расширения)
 */
internal fun buildAttachmentFile(conversationId: String, originalFileName: String): File {
    val extension = originalFileName.substringAfterLast('.', "txt").lowercase()
    val safeExtension = if (extension in SUPPORTED_TEXT_EXTENSIONS) extension else "txt"
    return File("message_attachments/$conversationId/${UUID.randomUUID()}.$safeExtension")
}
