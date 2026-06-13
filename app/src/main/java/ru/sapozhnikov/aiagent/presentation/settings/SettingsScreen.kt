package ru.sapozhnikov.aiagent.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.ui.theme.AiAgentTheme

/**
 * Точка входа экрана настроек: связывает [SettingsViewModel] с UI.
 *
 * @param onBack колбэк возврата на предыдущий экран
 */
@Composable
internal fun SettingsRoot(
    onBack: () -> Unit,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onBack = onBack,
        onContextManagementStrategyChanged = viewModel::onContextManagementStrategyChanged,
    )
}

/** Экран настроек приложения. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onContextManagementStrategyChanged: (ContextManagementStrategy) -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
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
                .padding(innerPadding),
        ) {
            ContextManagementStrategySetting(
                selectedStrategy = uiState.contextManagementStrategy,
                onStrategyChanged = onContextManagementStrategyChanged,
            )
        }
    }
}

/** Выбор стратегии управления контекстом. */
@Composable
private fun ContextManagementStrategySetting(
    selectedStrategy: ContextManagementStrategy,
    onStrategyChanged: (ContextManagementStrategy) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .selectableGroup(),
    ) {
        Text(
            text = "Управление контекстом",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Выберите стратегию подготовки истории для LLM API",
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ContextManagementStrategy.entries.forEach { strategy ->
            StrategyOption(
                strategy = strategy,
                selected = selectedStrategy == strategy,
                onSelected = { onStrategyChanged(strategy) },
            )
        }

        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun StrategyOption(
    strategy: ContextManagementStrategy,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.RadioButton,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = strategy.toDisplayTitle(),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = strategy.toDisplayDescription(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun ContextManagementStrategy.toDisplayTitle(): String = when (this) {
    ContextManagementStrategy.DEFAULT -> "По умолчанию"
    ContextManagementStrategy.SUMMARY_COMPRESSION -> "Summary Compression"
    ContextManagementStrategy.SLIDING_WINDOW -> "Sliding Window"
    ContextManagementStrategy.STICKY_FACTS -> "Sticky Facts"
    ContextManagementStrategy.BRANCHING -> "Branching"
}

private fun ContextManagementStrategy.toDisplayDescription(): String = when (this) {
    ContextManagementStrategy.DEFAULT -> "Полная история без обрезки"
    ContextManagementStrategy.SUMMARY_COMPRESSION -> "Последние 5 сообщений + резюме старой истории"
    ContextManagementStrategy.SLIDING_WINDOW -> "Только последние 10 сообщений"
    ContextManagementStrategy.STICKY_FACTS -> "Блок фактов + последние 10 сообщений"
    ContextManagementStrategy.BRANCHING -> "Независимые ветки от checkpoint"
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    AiAgentTheme {
        SettingsScreen(
            uiState = SettingsUiState(contextManagementStrategy = ContextManagementStrategy.SLIDING_WINDOW),
            onBack = {},
            onContextManagementStrategyChanged = {},
        )
    }
}
