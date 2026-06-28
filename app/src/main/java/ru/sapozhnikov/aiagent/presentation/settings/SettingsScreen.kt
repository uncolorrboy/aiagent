package ru.sapozhnikov.aiagent.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.sapozhnikov.aiagent.domain.model.AssistantInvariant
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance
import ru.sapozhnikov.aiagent.ui.theme.AiAgentTheme

/**
 * Точка входа экрана настроек: связывает [SettingsViewModel] с UI.
 *
 * @param onBack колбэк возврата на предыдущий экран
 * @param onOpenMemoryEditor колбэк открытия экрана редактирования памяти
 */
@Composable
internal fun SettingsRoot(
    onBack: () -> Unit,
    onOpenMemoryEditor: (MemoryEditorType, String?) -> Unit,
    onOpenInvariantEditor: (String?) -> Unit,
    onOpenMcpServers: () -> Unit,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onBack = onBack,
        onContextManagementStrategyChanged = viewModel::onContextManagementStrategyChanged,
        onAddWorkingMemoryClicked = { onOpenMemoryEditor(MemoryEditorType.WORKING, null) },
        onAddProfileMemoryClicked = { onOpenMemoryEditor(MemoryEditorType.PROFILE, null) },
        onEditWorkingMemory = { instance ->
            onOpenMemoryEditor(MemoryEditorType.WORKING, instance.id)
        },
        onEditProfileMemory = { instance ->
            onOpenMemoryEditor(MemoryEditorType.PROFILE, instance.id)
        },
        onDeleteWorkingMemory = viewModel::onDeleteWorkingMemory,
        onDeleteProfileMemory = viewModel::onDeleteProfileMemory,
        onAddInvariantClicked = { onOpenInvariantEditor(null) },
        onEditInvariant = { invariant -> onOpenInvariantEditor(invariant.id) },
        onDeleteInvariant = viewModel::onDeleteInvariant,
        onMcpEnabledChanged = viewModel::onMcpEnabledChanged,
        onOpenMcpServers = onOpenMcpServers,
    )
}

/** Экран настроек приложения. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onContextManagementStrategyChanged: (ContextManagementStrategy) -> Unit,
    onAddWorkingMemoryClicked: () -> Unit,
    onAddProfileMemoryClicked: () -> Unit,
    onEditWorkingMemory: (MemoryInstance) -> Unit,
    onEditProfileMemory: (MemoryInstance) -> Unit,
    onDeleteWorkingMemory: (String) -> Unit,
    onDeleteProfileMemory: (String) -> Unit,
    onAddInvariantClicked: () -> Unit,
    onEditInvariant: (AssistantInvariant) -> Unit,
    onDeleteInvariant: (String) -> Unit,
    onMcpEnabledChanged: (Boolean) -> Unit,
    onOpenMcpServers: () -> Unit,
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            McpServersEntry(
                mcpEnabled = uiState.mcpEnabled,
                serverCount = uiState.mcpServerCount,
                connectedCount = uiState.mcpConnectedCount,
                onMcpEnabledChanged = onMcpEnabledChanged,
                onOpenMcpServers = onOpenMcpServers,
            )

            ContextManagementStrategySetting(
                selectedStrategy = uiState.contextManagementStrategy,
                onStrategyChanged = onContextManagementStrategyChanged,
            )

            MemoryInstancesSection(
                title = "Рабочая память",
                description = "Данные текущей задачи. Экземпляры выбираются в чате.",
                instances = uiState.workingMemoryInstances,
                onAddClicked = onAddWorkingMemoryClicked,
                onEditClicked = onEditWorkingMemory,
                onDeleteClicked = onDeleteWorkingMemory,
            )

            MemoryInstancesSection(
                title = "Профиль",
                description = "Профиль, решения и знания. Экземпляры выбираются в чате.",
                instances = uiState.profileMemoryInstances,
                onAddClicked = onAddProfileMemoryClicked,
                onEditClicked = onEditProfileMemory,
                onDeleteClicked = onDeleteProfileMemory,
            )

            InvariantsSection(
                invariants = uiState.invariants,
                onAddClicked = onAddInvariantClicked,
                onEditClicked = onEditInvariant,
                onDeleteClicked = onDeleteInvariant,
            )
        }
    }
}

/** Компактная секция перехода к управлению MCP-серверами. */
@Composable
private fun McpServersEntry(
    mcpEnabled: Boolean,
    serverCount: Int,
    connectedCount: Int,
    onMcpEnabledChanged: (Boolean) -> Unit,
    onOpenMcpServers: () -> Unit,
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
                text = "MCP-серверы",
                style = MaterialTheme.typography.titleMedium,
            )
            Switch(
                checked = mcpEnabled,
                onCheckedChange = onMcpEnabledChanged,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenMcpServers)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        serverCount == 0 -> "Настроить подключения"
                        connectedCount > 0 -> "Подключено $connectedCount из $serverCount"
                        else -> "Серверов: $serverCount"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Управление внешними MCP-серверами и инструментами",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Открыть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
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
private fun InvariantsSection(
    invariants: List<AssistantInvariant>,
    onAddClicked: () -> Unit,
    onEditClicked: (AssistantInvariant) -> Unit,
    onDeleteClicked: (String) -> Unit,
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
                text = "Инварианты",
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onAddClicked) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Добавить",
                )
            }
        }
        Text(
            text = "Обязательные правила, которые ассистент не имеет права нарушать. Выбираются в чате.",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (invariants.isEmpty()) {
            Text(
                text = "Нет инвариантов",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            invariants.forEach { invariant ->
                InvariantItem(
                    invariant = invariant,
                    onEditClicked = { onEditClicked(invariant) },
                    onDeleteClicked = { onDeleteClicked(invariant.id) },
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun InvariantItem(
    invariant: AssistantInvariant,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = invariant.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = invariant.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onEditClicked) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Редактировать",
            )
        }
        IconButton(onClick = onDeleteClicked) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Удалить",
            )
        }
    }
}

@Composable
private fun MemoryInstancesSection(
    title: String,
    description: String,
    instances: List<MemoryInstance>,
    onAddClicked: () -> Unit,
    onEditClicked: (MemoryInstance) -> Unit,
    onDeleteClicked: (String) -> Unit,
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onAddClicked) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Добавить",
                )
            }
        }
        Text(
            text = description,
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (instances.isEmpty()) {
            Text(
                text = "Нет экземпляров",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            instances.forEach { instance ->
                MemoryInstanceItem(
                    instance = instance,
                    onEditClicked = { onEditClicked(instance) },
                    onDeleteClicked = { onDeleteClicked(instance.id) },
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun MemoryInstanceItem(
    instance: MemoryInstance,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = instance.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = instance.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onEditClicked) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Редактировать",
            )
        }
        IconButton(onClick = onDeleteClicked) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Удалить",
            )
        }
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
            uiState = SettingsUiState(
                contextManagementStrategy = ContextManagementStrategy.SLIDING_WINDOW,
                workingMemoryInstances = listOf(
                    MemoryInstance("1", "Задача A", "Описание задачи"),
                ),
                mcpEnabled = true,
                mcpServerCount = 2,
                mcpConnectedCount = 1,
            ),
            onBack = {},
            onContextManagementStrategyChanged = {},
            onAddWorkingMemoryClicked = {},
            onAddProfileMemoryClicked = {},
            onEditWorkingMemory = {},
            onEditProfileMemory = {},
            onDeleteWorkingMemory = {},
            onDeleteProfileMemory = {},
            onAddInvariantClicked = {},
            onEditInvariant = {},
            onDeleteInvariant = {},
            onMcpEnabledChanged = {},
            onOpenMcpServers = {},
        )
    }
}
