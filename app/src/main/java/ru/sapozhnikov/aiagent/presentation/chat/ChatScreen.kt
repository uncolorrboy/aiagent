package ru.sapozhnikov.aiagent.presentation.chat

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.sapozhnikov.aiagent.presentation.formatter.formatChatPrice
import ru.sapozhnikov.aiagent.presentation.formatter.formatTokenCount
import ru.sapozhnikov.aiagent.ui.theme.AiAgentTheme

@Composable
internal fun ChatRoot(onOpenChatList: () -> Unit) {
    val viewModel: ChatViewModel = hiltViewModel()
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.errorEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    ChatScreen(
        uiState = uiState,
        onSendMessageButtonClicked = viewModel::onMessageSent,
        onTextFileSelected = viewModel::onTextFileSelected,
        onOpenChatList = onOpenChatList,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(
    uiState: ChatScreenUiState,
    onSendMessageButtonClicked: (String) -> Unit,
    onTextFileSelected: (Uri) -> Unit,
    onOpenChatList: () -> Unit,
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val textFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onTextFileSelected(uri)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Чат")
                        if (uiState.totalTokenCount > 0) {
                            Text(
                                text = "Всего: ${formatTokenCount(uiState.totalTokenCount)}, ${formatChatPrice(uiState.chatPrice)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenChatList) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "К списку чатов",
                        )
                    }
                },
            )
        },
        containerColor = Color.White,
    ) { innerPadding ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .windowInsetsPadding(
                    WindowInsets.ime
                        .union(WindowInsets.navigationBars)
                        .only(WindowInsetsSides.Bottom),
                ),
        ) {
            val (chatRef, textInputRef, attachFileButtonRef, sendButtonRef, emptyChatPlaceholderRef) = createRefs()

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
                        end.linkTo(attachFileButtonRef.start)
                        bottom.linkTo(parent.bottom)

                        width = Dimension.fillToConstraints
                    },
                value = textInput,
                onValueChange = { newValue -> textInput = newValue },
                placeholder = { Text("Введите сообщение...") }
            )

            IconButton(
                modifier = Modifier.constrainAs(attachFileButtonRef) {
                    end.linkTo(sendButtonRef.start)
                    top.linkTo(textInputRef.top)
                    bottom.linkTo(textInputRef.bottom)
                },
                enabled = !uiState.isLoading,
                onClick = {
                    textFilePickerLauncher.launch(SUPPORTED_TEXT_FILE_MIME_TYPES)
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.AttachFile,
                    contentDescription = "Отправить текстовый файл",
                )
            }

            IconButton(
                modifier = Modifier.constrainAs(sendButtonRef) {
                    end.linkTo(parent.end)
                    top.linkTo(textInputRef.top)
                    bottom.linkTo(textInputRef.bottom)
                },
                enabled = textInput.isNotBlank() && !uiState.isLoading,
                onClick = {
                    onSendMessageButtonClicked(textInput)
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
            ChatMessageContent(
                text = chatMessage.text,
                messageOwner = chatMessage.messageOwner,
                messageKind = chatMessage.kind,
                attachmentFileName = chatMessage.attachmentFileName,
                textColor = messageTextColor,
            )
            Text(
                text = buildMessageFooter(chatMessage),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = timeTextColor,
            )
        }
    }
}

private fun buildMessageFooter(chatMessage: ChatMessage): String {
    val cacheHitTokensAppend = " + кэш".takeIf {
        chatMessage.messageOwner == MessageOwner.USER
    }.orEmpty()
    return if (chatMessage.tokenCount != null) {
        "${chatMessage.time} · ${formatTokenCount(chatMessage.tokenCount)}$cacheHitTokensAppend"
    } else {
        chatMessage.time
    }
}

@Preview
@Composable
private fun ChatItemPreview() {
    AiAgentTheme {
        Column {
            ChatItem(
                chatMessage = ChatMessage(
                    text = "Привет! Как дела?",
                    time = "14:32",
                    messageOwner = MessageOwner.USER,
                    tokenCount = 12,
                ),
            )
            ChatItem(
                chatMessage = ChatMessage(
                    text = """
                        Вот пример **Markdown**:
                        - пункт 1
                        - пункт 2
                        ```kotlin
                        val x = 42
                        ```
                    """.trimIndent(),
                    time = "14:33",
                    messageOwner = MessageOwner.AI,
                    tokenCount = 48,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun ChatScreenPreview() {
    AiAgentTheme {
        ChatScreen(
            uiState = ChatScreenUiState(
                totalTokenCount = 156,
                chatPrice = 0.03,
                items = listOf(
                    ChatMessage(
                        text = "Привет! Чем могу помочь?",
                        time = "14:30",
                        messageOwner = MessageOwner.AI,
                        tokenCount = 42,
                    ),
                    ChatMessage(
                        text = "Расскажи про ConstraintLayout в Compose",
                        time = "14:31",
                        messageOwner = MessageOwner.USER,
                        tokenCount = 114,
                    ),
                ),
            ),
            onSendMessageButtonClicked = {},
            onTextFileSelected = {},
            onOpenChatList = {},
        )
    }
}

private val SUPPORTED_TEXT_FILE_MIME_TYPES = arrayOf(
    "text/plain",
    "text/markdown",
    "text/x-markdown",
)






