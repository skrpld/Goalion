package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.presentation.screens.timeline.TimelineDateUtils.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBottomSheet(
    state: DialogState,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Long, Int) -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(state.initialTitle) }
    var priority by remember { mutableIntStateOf(state.initialPriority) }
    var startDate by remember { mutableLongStateOf(state.initialStartDate) }
    var targetDate by remember { mutableLongStateOf(state.initialTargetDate) }

    val isReadOnly = state.mode == EditMode.VIEW_TASK

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isReadOnly) "Task Details" else "Edit",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                readOnly = isReadOnly,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isReadOnly) {
                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p.value,
                            onClick = { priority = p.value },
                            label = { Text(p.name) },
                            leadingIcon = { Icon(Icons.Default.Circle, null, tint = p.color) }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formatDateTime(startDate),
                    onValueChange = {},
                    label = { Text("Start") },
                    readOnly = true,
                    enabled = !isReadOnly,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !isReadOnly) {}
                )
                OutlinedTextField(
                    value = formatDateTime(targetDate),
                    onValueChange = {},
                    label = { Text("End") },
                    readOnly = true,
                    enabled = !isReadOnly,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !isReadOnly) {}
                )
            }

            if (!isReadOnly) {
                Button(
                    onClick = { onSave(title, "", startDate, targetDate, priority) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}