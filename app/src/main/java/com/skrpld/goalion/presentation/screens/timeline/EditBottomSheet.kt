package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import java.util.Calendar

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
    var description by remember { mutableStateOf(state.initialDescription) }
    var priority by remember { mutableIntStateOf(state.initialPriority) }
    var startDate by remember { mutableLongStateOf(state.initialStartDate) }
    var targetDate by remember { mutableLongStateOf(state.initialTargetDate) }

    var pickingDateFor by remember { mutableStateOf<String?>(null) }

    val isReadOnly = state.mode == EditMode.VIEW_TASK

    if (pickingDateFor != null) {
        DateTimePickerModal(
            initialMillis = if (pickingDateFor == "start") startDate else targetDate,
            onDismiss = { pickingDateFor = null },
            onConfirm = { newMillis ->
                if (pickingDateFor == "start") startDate = newMillis else targetDate = newMillis
                pickingDateFor = null
            }
        )
    }

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

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                readOnly = isReadOnly,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
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
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = TimelineDateUtils.formatDateTime(startDate),
                        onValueChange = {},
                        label = { Text("Start") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isReadOnly) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { pickingDateFor = "start" }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = TimelineDateUtils.formatDateTime(targetDate),
                        onValueChange = {},
                        label = { Text("End") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isReadOnly) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { pickingDateFor = "target" }
                        )
                    }
                }
            }

            if (!isReadOnly) {
                Button(
                    onClick = { onSave(title, description, startDate, targetDate, priority) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerModal(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    val initialCal = Calendar.getInstance().apply { timeInMillis = initialMillis }
    val timePickerState = rememberTimePickerState(
        initialHour = initialCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCal.get(Calendar.MINUTE),
        is24Hour = true
    )

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { showTimePicker = true }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    val dateMillis = datePickerState.selectedDateMillis ?: initialMillis
                    val finalCal = Calendar.getInstance().apply {
                        timeInMillis = dateMillis
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    onConfirm(finalCal.timeInMillis)
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Back") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}