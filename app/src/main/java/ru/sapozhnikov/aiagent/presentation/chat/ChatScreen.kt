package ru.sapozhnikov.aiagent.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.sapozhnikov.aiagent.ui.theme.AiAgentTheme

@Composable
internal fun ChatRoot() {
    val viewModel: ChatViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        uiState = uiState,
        onSendMessage = viewModel::onMessageSent,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatScreen(uiState: ChatScreenUiState, onSendMessage: (String) -> Unit) {
    val listState = rememberLazyListState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(
                WindowInsets.ime
                    .union(WindowInsets.navigationBars)
                    .only(WindowInsetsSides.Bottom),
            ),
    ) {
        val (chatRef, textInputRef, sendButtonRef, emptyChatPlaceholderRef) = createRefs()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(chatRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(textInputRef.top)
                    height = Dimension.fillToConstraints
                },
            reverseLayout = true,
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
        ) {
            items(uiState.items) { chatMessage ->
                ChatItem(chatMessage)
            }
        }

        var textInput by remember { mutableStateOf("") }

        OutlinedTextField(
            modifier = Modifier
                .constrainAs(textInputRef) {
                    start.linkTo(parent.start, 16.dp)
                    end.linkTo(sendButtonRef.start)
                    bottom.linkTo(parent.bottom, 16.dp)

                    width = Dimension.fillToConstraints
                },
            value = textInput,
            onValueChange = { newValue -> textInput = newValue },
            placeholder = { Text("Введите сообщение...") }
        )

        IconButton(
            modifier = Modifier.constrainAs(sendButtonRef) {
                end.linkTo(parent.end)
                top.linkTo(textInputRef.top)
                bottom.linkTo(textInputRef.bottom)
            },
            enabled = textInput.isNotBlank() && !uiState.isLoading,
            onClick = {
                onSendMessage(textInput)
                textInput = ""
            },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Отправить сообщение"

            )
        }

        Text(
            text = "Что сегодня у тебя на уме?",
            modifier = Modifier
                .constrainAs(emptyChatPlaceholderRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(textInputRef.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                    visibility = if (uiState.items.isEmpty()) {
                        Visibility.Visible
                    } else {
                        Visibility.Gone
                    }
                }
                .padding(horizontal = 32.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ChatItem(chatMessage: ChatMessage) {
    val isUserMessage = chatMessage.messageOwner == MessageOwner.USER
    val bubbleColor = if (isUserMessage) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val messageTextColor = if (isUserMessage) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val timeTextColor = if (isUserMessage) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUserMessage) 16.dp else 4.dp,
                        bottomEnd = if (isUserMessage) 4.dp else 16.dp,
                    ),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = chatMessage.text,
                style = MaterialTheme.typography.bodyLarge,
                color = messageTextColor,
            )
            Text(
                text = chatMessage.time,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = timeTextColor,
            )
        }
    }
}

@Preview
@Composable
private fun ChatItemPreview() {
    AiAgentTheme {
        ChatItem(
            chatMessage = ChatMessage(
                text = "Привет! Как дела?",
                time = "14:32",
                messageOwner = MessageOwner.USER,
            ),
        )
    }
}

@Preview
@Composable
private fun ChatScreenPreview() {
    AiAgentTheme {
        ChatScreen(
            uiState = ChatScreenUiState(
                items = listOf(
                    ChatMessage(
                        text = "Привет! Чем могу помочь?",
                        time = "14:30",
                        messageOwner = MessageOwner.AI,
                    ),
                    ChatMessage(
                        text = "Расскажи про ConstraintLayout в Compose",
                        time = "14:31",
                        messageOwner = MessageOwner.USER,
                    ),
                ),
            ),
            onSendMessage = {},
        )
    }
}






