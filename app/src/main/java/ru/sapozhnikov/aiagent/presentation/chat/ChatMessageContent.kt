package ru.sapozhnikov.aiagent.presentation.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import dev.jeziellago.compose.markdowntext.MarkdownText

/**
 * Отображает содержимое сообщения в зависимости от типа:
 * Markdown для ответов AI, имя файла для вложений, обрезанный текст для пользователя.
 *
 * @param text текст сообщения
 * @param messageOwner отправитель — пользователь или ассистент
 * @param messageKind тип содержимого — текст или файл
 * @param attachmentFileName имя прикреплённого файла
 * @param textColor цвет текста в пузыре сообщения
 */
@Composable
internal fun ChatMessageContent(
    text: String,
    messageOwner: MessageOwner,
    messageKind: MessageKind,
    attachmentFileName: String?,
    textColor: Color,
) {
    when {
        messageKind == MessageKind.FILE && !attachmentFileName.isNullOrBlank() -> {
            FileMessageContent(
                fileName = attachmentFileName,
                textColor = textColor,
            )
        }
        messageOwner == MessageOwner.AI -> {
            MarkdownText(
                markdown = text,
                style = MaterialTheme.typography.bodyLarge.merge(
                    TextStyle(color = textColor),
                ),
                linkColor = MaterialTheme.colorScheme.primary,
            )
        }
        else -> {
            Text(
                text = truncateUserMessageForDisplay(text),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )
        }
    }
}
