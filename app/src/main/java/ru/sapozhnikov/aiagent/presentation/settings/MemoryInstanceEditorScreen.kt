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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.sapozhnikov.aiagent.ui.theme.AiAgentTheme

/**
 * Точка входа экрана редактирования экземпляра памяти.
 *
 * @param onBack колбэк возврата на предыдущий экран
 */
@Composable
internal fun MemoryInstanceEditorRoot(
    onBack: () -> Unit,
) {
    val viewModel: MemoryInstanceEditorViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saveCompleted by viewModel.saveCompleted.collectAsStateWithLifecycle()

    LaunchedEffect(saveCompleted) {
        if (saveCompleted) {
            onBack()
        }
    }

    MemoryInstanceEditorScreen(
        uiState = uiState,
        onBack = onBack,
        onNameChanged = viewModel::onNameChanged,
        onTextChanged = viewModel::onTextChanged,
        onSaveClicked = viewModel::onSaveClicked,
    )
}

/** Полноэкранный редактор экземпляра памяти. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoryInstanceEditorScreen(
    uiState: MemoryInstanceEditorUiState,
    onBack: () -> Unit,
    onNameChanged: (String) -> Unit,
    onTextChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
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
                        enabled = uiState.canSave,
                        onClick = onSaveClicked,
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
                CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .imePadding(),
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChanged,
                    label = { Text("Название") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true,
                    enabled = !uiState.isSaving,
                )
                OutlinedTextField(
                    value = uiState.text,
                    onValueChange = onTextChanged,
                    label = { Text("Текст") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    enabled = !uiState.isSaving,
                )
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 12.dp),
                    )
                } else {
                    Text(
                        text = "Введите содержимое памяти — профиль, задачу, контекст или знания.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MemoryInstanceEditorScreenPreview() {
    AiAgentTheme {
        MemoryInstanceEditorScreen(
            uiState = MemoryInstanceEditorUiState(
                memoryType = MemoryEditorType.WORKING,
                name = "Задача A",
                text = "Описание текущей задачи и контекста работы.",
            ),
            onBack = {},
            onNameChanged = {},
            onTextChanged = {},
            onSaveClicked = {},
        )
    }
}
