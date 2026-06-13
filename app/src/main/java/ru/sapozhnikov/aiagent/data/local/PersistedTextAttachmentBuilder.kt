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

internal data class PersistedTextAttachment(
    val fileName: String,
    val content: String,
    val storedUri: String,
)

@Singleton
internal class PersistedTextAttachmentBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
) {

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

    suspend fun readStoredContent(storedUri: String): String = withContext(Dispatchers.IO) {
        val file = storedUri.toStoredFile()
        if (!file.exists()) {
            error("Файл сообщения не найден")
        }
        file.readText()
    }

    suspend fun deleteConversationAttachments(conversationId: String) = withContext(Dispatchers.IO) {
        File(context.filesDir, "message_attachments/$conversationId").deleteRecursively()
    }
}
