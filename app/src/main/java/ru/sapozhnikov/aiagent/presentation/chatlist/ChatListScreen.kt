package ru.sapozhnikov.aiagent.presentation.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.sapozhnikov.aiagent.domain.model.ConversationMode
import ru.sapozhnikov.aiagent.ui.theme.AiAgentTheme

/**
 * Точка входа экрана списка чатов: связывает [ChatListViewModel] с UI.
 *
 * @param onConversationClick колбэк открытия существующего диалога
 * @param onNewChatClick колбэк создания нового диалога
 * @param onNewTaskClick колбэк создания новой задачи
 * @param onSettingsClick колбэк перехода к экрану настроек
 */
@Composable
internal fun ChatListRoot(
    onConversationClick: (String, Boolean) -> Unit,
    onNewChatClick: () -> Unit,
    onNewTaskClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val viewModel: ChatListViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatListScreen(
        uiState = uiState,
        onConversationClick = onConversationClick,
        onConversationDelete = viewModel::deleteConversation,
        onNewChatClick = onNewChatClick,
        onNewTaskClick = onNewTaskClick,
        onSettingsClick = onSettingsClick,
    )
}

/** Экран списка сохранённых диалогов с возможностью создания и удаления. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListScreen(
    uiState: ChatListUiState,
    onConversationClick: (String, Boolean) -> Unit,
    onConversationDelete: (String) -> Unit,
    onNewChatClick: () -> Unit,
    onNewTaskClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            TopAppBar(
                title = { Text("История чатов") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FloatingActionButton(
                    onClick = onNewTaskClick,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Новая задача",
                    )
                }
                FloatingActionButton(onClick = onNewChatClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Новый чат",
                    )
                }
            }
        },
        containerColor = Color.White,
    ) { innerPadding ->
        if (uiState.conversations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Пока нет сохранённых чатов",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Нажмите +, чтобы начать новый чат",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(uiState.conversations, key = { it.id }) { conversation ->
                    ConversationListItem(
                        conversation = conversation,
                        onClick = {
                            onConversationClick(
                                conversation.id,
                                conversation.mode == ConversationMode.TASK,
                            )
                        },
                        onDelete = { onConversationDelete(conversation.id) },
                    )
                }
            }
        }
    }
}

/** Элемент списка: заголовок диалога, дата обновления и кнопка удаления. */
@Composable
private fun ConversationListItem(
    conversation: ConversationItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (conversation.mode == ConversationMode.TASK) {
                    Surface(
                        modifier = Modifier.padding(start = 8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            text = "Задача",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
            Text(
                text = conversation.updatedAt,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Удалить чат",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun ChatListScreenPreview() {
    AiAgentTheme {
        ChatListScreen(
            uiState = ChatListUiState(
                conversations = listOf(
                    ConversationItem(
                        id = "1",
                        title = "Расскажи про Kotlin Coroutines",
                        updatedAt = "10 июн, 14:30",
                    ),
                    ConversationItem(
                        id = "2",
                        title = "Новый чат",
                        updatedAt = "9 июн, 18:15",
                    ),
                ),
            ),
            onConversationClick = { _, _ -> },
            onConversationDelete = {},
            onNewChatClick = {},
            onNewTaskClick = {},
            onSettingsClick = {},
        )
    }
}
