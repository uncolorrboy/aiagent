package ru.sapozhnikov.aiagent.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.sapozhnikov.aiagent.domain.model.McpAuthType
import ru.sapozhnikov.aiagent.domain.model.McpServerConfig
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition
import ru.sapozhnikov.aiagent.ui.theme.AiAgentTheme

@Composable
internal fun McpServersRoot(
    onBack: () -> Unit,
    onOpenServerEditor: (String?) -> Unit,
) {
    val viewModel: McpServersViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    McpServersScreen(
        uiState = uiState,
        onBack = onBack,
        onMcpEnabledChanged = viewModel::onMcpEnabledChanged,
        onServerEnabledChanged = viewModel::onServerEnabledChanged,
        onToolsExpandedToggled = viewModel::onToolsExpandedToggled,
        onConnectClicked = viewModel::onConnectClicked,
        onAuthorizeClicked = viewModel::onAuthorizeClicked,
        onDisconnectClicked = viewModel::onDisconnectClicked,
        onDeleteServer = viewModel::onDeleteServer,
        onAddServerClicked = { onOpenServerEditor(null) },
        onEditServerClicked = { serverId -> onOpenServerEditor(serverId) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun McpServersScreen(
    uiState: McpServersUiState,
    onBack: () -> Unit,
    onMcpEnabledChanged: (Boolean) -> Unit,
    onServerEnabledChanged: (String, Boolean) -> Unit,
    onToolsExpandedToggled: (String) -> Unit,
    onConnectClicked: (String) -> Unit,
    onAuthorizeClicked: (String) -> Unit,
    onDisconnectClicked: (String) -> Unit,
    onDeleteServer: (String) -> Unit,
    onAddServerClicked: () -> Unit,
    onEditServerClicked: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            TopAppBar(
                title = { Text("MCP-серверы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddServerClicked) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Добавить сервер",
                        )
                    }
                },
            )
        },
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            McpGlobalToggle(
                mcpEnabled = uiState.mcpEnabled,
                connectedCount = uiState.connectedCount,
                totalCount = uiState.servers.size,
                onMcpEnabledChanged = onMcpEnabledChanged,
            )

            if (uiState.servers.isEmpty()) {
                Text(
                    text = "Нет добавленных серверов",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                uiState.servers.forEach { serverItem ->
                    McpServerCard(
                        item = serverItem,
                        mcpEnabled = uiState.mcpEnabled,
                        onServerEnabledChanged = onServerEnabledChanged,
                        onToolsExpandedToggled = onToolsExpandedToggled,
                        onConnectClicked = onConnectClicked,
                        onAuthorizeClicked = onAuthorizeClicked,
                        onDisconnectClicked = onDisconnectClicked,
                        onDeleteServer = onDeleteServer,
                        onEditServerClicked = onEditServerClicked,
                    )
                }
            }
        }
    }
}

@Composable
private fun McpGlobalToggle(
    mcpEnabled: Boolean,
    connectedCount: Int,
    totalCount: Int,
    onMcpEnabledChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Использовать MCP",
                style = MaterialTheme.typography.titleMedium,
            )
            Switch(
                checked = mcpEnabled,
                onCheckedChange = onMcpEnabledChanged,
            )
        }
        Text(
            text = if (totalCount == 0) {
                "Добавьте MCP-серверы для использования инструментов в чате"
            } else {
                "Подключено $connectedCount из $totalCount серверов"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun McpServerCard(
    item: McpServerItemUiState,
    mcpEnabled: Boolean,
    onServerEnabledChanged: (String, Boolean) -> Unit,
    onToolsExpandedToggled: (String) -> Unit,
    onConnectClicked: (String) -> Unit,
    onAuthorizeClicked: (String) -> Unit,
    onDisconnectClicked: (String) -> Unit,
    onDeleteServer: (String) -> Unit,
    onEditServerClicked: (String) -> Unit,
) {
    val server = item.server
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayTitle,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (server.serverName != null) {
                        Text(
                            text = server.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = authStatusText(server),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = connectionStatusText(item.connectionStatus),
                        style = MaterialTheme.typography.bodySmall,
                        color = connectionStatusColor(item.connectionStatus),
                    )
                }
                Switch(
                    checked = server.enabled,
                    onCheckedChange = { enabled ->
                        onServerEnabledChanged(server.id, enabled)
                    },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (item.connectionStatus == McpConnectionStatus.CONNECTING) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 4.dp))
                    Text(text = "Подключение…", style = MaterialTheme.typography.bodyMedium)
                } else if (item.connectionStatus == McpConnectionStatus.AUTHORIZING) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 4.dp))
                    Text(text = "OAuth…", style = MaterialTheme.typography.bodyMedium)
                } else if (item.connectionStatus == McpConnectionStatus.CONNECTED) {
                    OutlinedButton(
                        onClick = { onDisconnectClicked(server.id) },
                        enabled = mcpEnabled,
                    ) {
                        Text("Отключить")
                    }
                } else if (server.authType == McpAuthType.OAUTH && server.oauthTokens == null) {
                    Button(
                        onClick = { onAuthorizeClicked(server.id) },
                        enabled = mcpEnabled && server.enabled,
                    ) {
                        Text("Авторизоваться")
                    }
                    OutlinedButton(
                        onClick = { onConnectClicked(server.id) },
                        enabled = mcpEnabled && server.enabled,
                    ) {
                        Text("Подключить")
                    }
                } else {
                    Button(
                        onClick = { onConnectClicked(server.id) },
                        enabled = mcpEnabled && server.enabled,
                    ) {
                        Text("Подключить")
                    }
                }

                IconButton(onClick = { onEditServerClicked(server.id) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Редактировать")
                }
                IconButton(onClick = { onDeleteServer(server.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                }
            }

            item.connectionError?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (item.connectionStatus == McpConnectionStatus.CONNECTED || item.tools.isNotEmpty()) {
                McpToolsExpandableSection(
                    tools = item.tools,
                    expanded = item.toolsExpanded,
                    onToggle = { onToolsExpandedToggled(server.id) },
                )
            }
        }
    }
}

@Composable
private fun McpToolsExpandableSection(
    tools: List<McpToolDefinition>,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val toolsCount = tools.size
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (toolsCount == 0) {
                "Инструменты: нет"
            } else {
                "Инструменты ($toolsCount)"
            },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
        )
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) "Свернуть" else "Развернуть",
        )
    }

    AnimatedVisibility(visible = expanded) {
        Column(modifier = Modifier.padding(top = 4.dp)) {
            if (tools.isEmpty()) {
                Text(
                    text = "Сервер не предоставляет инструментов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                tools.forEach { tool ->
                    McpToolItem(tool = tool)
                }
            }
        }
    }
}

@Composable
private fun McpToolItem(tool: McpToolDefinition) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = tool.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        tool.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun authStatusText(server: McpServerConfig): String = when (server.authType) {
    McpAuthType.NONE -> "Авторизация: без токена"
    McpAuthType.BEARER -> "Авторизация: Bearer-токен"
    McpAuthType.OAUTH -> if (server.oauthTokens != null) {
        "Авторизация: OAuth (токен получен)"
    } else {
        "Авторизация: OAuth (требуется вход)"
    }
}

@Composable
private fun connectionStatusText(status: McpConnectionStatus): String = when (status) {
    McpConnectionStatus.DISCONNECTED -> "Не подключён"
    McpConnectionStatus.CONNECTING -> "Подключение…"
    McpConnectionStatus.AUTHORIZING -> "OAuth-авторизация…"
    McpConnectionStatus.CONNECTED -> "Подключён"
    McpConnectionStatus.ERROR -> "Ошибка подключения"
}

@Composable
private fun connectionStatusColor(status: McpConnectionStatus): Color = when (status) {
    McpConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primary
    McpConnectionStatus.AUTHORIZING -> MaterialTheme.colorScheme.tertiary
    McpConnectionStatus.ERROR -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Preview
@Composable
private fun McpServersScreenPreview() {
    AiAgentTheme {
        McpServersScreen(
            uiState = McpServersUiState(
                mcpEnabled = true,
                servers = listOf(
                    McpServerItemUiState(
                        server = McpServerConfig(
                            id = "1",
                            url = "http://10.0.2.2:3000/mcp",
                            serverName = "Test MCP",
                            tools = listOf(
                                McpToolDefinition("search", "Поиск", null),
                            ),
                        ),
                        connectionStatus = McpConnectionStatus.CONNECTED,
                    ),
                ),
            ),
            onBack = {},
            onMcpEnabledChanged = {},
            onServerEnabledChanged = { _, _ -> },
            onToolsExpandedToggled = {},
            onConnectClicked = {},
            onAuthorizeClicked = {},
            onDisconnectClicked = {},
            onDeleteServer = {},
            onAddServerClicked = {},
            onEditServerClicked = {},
        )
    }
}
