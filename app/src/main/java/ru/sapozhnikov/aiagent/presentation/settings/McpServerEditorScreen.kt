package ru.sapozhnikov.aiagent.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.sapozhnikov.aiagent.domain.model.McpAuthType
import ru.sapozhnikov.aiagent.ui.theme.AiAgentTheme

@Composable
internal fun McpServerEditorRoot(
    onBack: () -> Unit,
) {
    val viewModel: McpServerEditorViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saveCompleted by viewModel.saveCompleted.collectAsStateWithLifecycle()

    LaunchedEffect(saveCompleted) {
        if (saveCompleted) {
            onBack()
        }
    }

    McpServerEditorScreen(
        uiState = uiState,
        onBack = onBack,
        onUrlChanged = viewModel::onUrlChanged,
        onAuthTypeChanged = viewModel::onAuthTypeChanged,
        onAuthTokenChanged = viewModel::onAuthTokenChanged,
        onSaveClicked = viewModel::onSaveClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun McpServerEditorScreen(
    uiState: McpServerEditorUiState,
    onBack: () -> Unit,
    onUrlChanged: (String) -> Unit,
    onAuthTypeChanged: (McpAuthType) -> Unit,
    onAuthTokenChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(uiState.screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSaveClicked,
                        enabled = uiState.canSave,
                    ) {
                        Text("Сохранить")
                    }
                },
            )
        },
        containerColor = Color.White,
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            ) {
                OutlinedTextField(
                    value = uiState.url,
                    onValueChange = onUrlChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL сервера") },
                    placeholder = { Text("https://mcp.example.com/mcp") },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                )

                Text(
                    text = "Авторизация",
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    McpAuthType.entries.forEachIndexed { index, authType ->
                        SegmentedButton(
                            selected = uiState.authType == authType,
                            onClick = { onAuthTypeChanged(authType) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = McpAuthType.entries.size,
                            ),
                            enabled = !uiState.isSaving,
                        ) {
                            Text(authTypeLabel(authType))
                        }
                    }
                }

                when (uiState.authType) {
                    McpAuthType.NONE -> {
                        Text(
                            text = "Подключение без авторизации",
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    McpAuthType.BEARER -> {
                        OutlinedTextField(
                            value = uiState.authToken,
                            onValueChange = onAuthTokenChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            label = { Text("Bearer-токен") },
                            placeholder = { Text("Для защищённого сервера на VPS") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            enabled = !uiState.isSaving,
                        )
                    }
                    McpAuthType.OAUTH -> {
                        Text(
                            text = "При подключении откроется браузер для OAuth 2.1 авторизации по спецификации MCP",
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                uiState.errorMessage?.let { error ->
                    Text(
                        text = error,
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun authTypeLabel(authType: McpAuthType): String = when (authType) {
    McpAuthType.NONE -> "Нет"
    McpAuthType.BEARER -> "Token"
    McpAuthType.OAUTH -> "OAuth"
}

@Preview
@Composable
private fun McpServerEditorScreenPreview() {
    AiAgentTheme {
        McpServerEditorScreen(
            uiState = McpServerEditorUiState(authType = McpAuthType.OAUTH),
            onBack = {},
            onUrlChanged = {},
            onAuthTypeChanged = {},
            onAuthTokenChanged = {},
            onSaveClicked = {},
        )
    }
}
