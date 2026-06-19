package ru.sapozhnikov.aiagent.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.sapozhnikov.aiagent.domain.model.AssistantInvariant

/**
 * BottomSheet выбора инвариантов для диалога.
 *
 * Инварианты хранятся отдельно от истории и передаются ассистенту как обязательные правила.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatInvariantsBottomSheet(
    availableInvariants: List<AssistantInvariant>,
    selectedInvariantIds: Set<String>,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedIds by remember(selectedInvariantIds) { mutableStateOf(selectedInvariantIds) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Инварианты",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = "Выберите правила, которые ассистент обязан соблюдать в этом диалоге. " +
                    "Он откажется предлагать решения, нарушающие выбранные инварианты.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (availableInvariants.isEmpty()) {
                Text(
                    text = "Нет инвариантов. Создайте их в настройках приложения.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                availableInvariants.forEach { invariant ->
                    InvariantCheckboxItem(
                        invariant = invariant,
                        checked = invariant.id in selectedIds,
                        onCheckedChange = { checked ->
                            selectedIds = if (checked) {
                                selectedIds + invariant.id
                            } else {
                                selectedIds - invariant.id
                            }
                        },
                    )
                }
            }

            Button(
                onClick = { onSave(selectedIds) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                Text("Сохранить")
            }
        }
    }
}

@Composable
private fun InvariantCheckboxItem(
    invariant: AssistantInvariant,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = invariant.name,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = invariant.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
