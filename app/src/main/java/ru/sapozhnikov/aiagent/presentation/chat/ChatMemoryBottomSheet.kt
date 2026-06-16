package ru.sapozhnikov.aiagent.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance

/**
 * BottomSheet выбора рабочей и долговременной памяти для диалога.
 *
 * Выбор сохраняется один раз на диалог; после сохранения поля блокируются.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatMemoryBottomSheet(
    workingMemoryInstances: List<MemoryInstance>,
    profileMemoryInstances: List<MemoryInstance>,
    selectedWorkingMemoryId: String?,
    selectedProfileMemoryId: String?,
    isSelectionLocked: Boolean,
    onDismiss: () -> Unit,
    onSave: (workingMemoryId: String?, profileMemoryId: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var workingId by remember(selectedWorkingMemoryId) { mutableStateOf(selectedWorkingMemoryId) }
    var profileId by remember(selectedProfileMemoryId) { mutableStateOf(selectedProfileMemoryId) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Память диалога",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = if (isSelectionLocked) {
                    "Память выбрана для этого диалога и не может быть изменена"
                } else {
                    "Выберите рабочую и долговременную память. Выбор сохраняется один раз."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            MemoryDropdown(
                label = "Рабочая память",
                instances = workingMemoryInstances,
                selectedId = workingId,
                enabled = !isSelectionLocked,
                onSelected = { workingId = it },
            )

            MemoryDropdown(
                label = "Долговременная память",
                instances = profileMemoryInstances,
                selectedId = profileId,
                enabled = !isSelectionLocked,
                modifier = Modifier.padding(top = 12.dp),
                onSelected = { profileId = it },
            )

            if (!isSelectionLocked) {
                Button(
                    onClick = { onSave(workingId, profileId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoryDropdown(
    label: String,
    instances: List<MemoryInstance>,
    selectedId: String?,
    enabled: Boolean,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf<MemoryInstance?>(null) + instances
    val selectedLabel = instances.find { it.id == selectedId }?.name ?: "Нет"

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = enabled)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                val optionLabel = option?.name ?: "Нет"
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = {
                        onSelected(option?.id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
