package ru.sapozhnikov.aiagent.presentation.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import ru.sapozhnikov.aiagent.ui.theme.AiAgentTheme

@Composable
internal fun ChatListRoot(
    onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
) {
    val viewModel: ChatListViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatListScreen(
        uiState = uiState,
        onConversationClick = onConversationClick,
        onNewChatClick = onNewChatClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListScreen(
    uiState: ChatListUiState,
    onConversationClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            TopAppBar(title = { Text("История чатов") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewChatClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Новый чат",
                )
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
                        onClick = { onConversationClick(conversation.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationListItem(
    conversation: ConversationItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = conversation.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = conversation.updatedAt,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
            onConversationClick = {},
            onNewChatClick = {},
        )
    }
}
