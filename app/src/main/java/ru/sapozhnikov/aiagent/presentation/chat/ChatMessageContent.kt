package ru.sapozhnikov.aiagent.presentation.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
internal fun ChatMessageContent(
    text: String,
    messageOwner: MessageOwner,
    textColor: Color,
) {
    if (messageOwner == MessageOwner.AI) {
        MarkdownText(
            markdown = text,
            style = MaterialTheme.typography.bodyLarge.merge(
                TextStyle(color = textColor),
            ),
            linkColor = MaterialTheme.colorScheme.primary,
        )
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}
