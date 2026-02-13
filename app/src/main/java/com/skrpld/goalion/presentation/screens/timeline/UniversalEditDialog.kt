package com.skrpld.goalion.presentation.screens.timeline

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalEditDialog(
    state: DialogState,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Long) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(state.initialTitle) }
    var description by remember { mutableStateOf(state.initialDescription) }
    var startDate by remember { mutableLongStateOf(state.initialStartDate) }
    var targetDate by remember { mutableLongStateOf(state.initialTargetDate) }

    val dialogTitle = when(state.mode) {
        EditMode.CREATE_GOAL -> "Add Goal"
        EditMode.EDIT_GOAL -> "Edit Goal"
        EditMode.CREATE_TASK -> "Add Task"
        EditMode.EDIT_TASK -> "Edit Task"
    }

    val isEditing = state.mode == EditMode.EDIT_GOAL || state.mode == EditMode.EDIT_TASK
    val dateFlags = if (isEditing) DateUtils.FORMAT_SHOW_DATE else DateUtils.FORMAT_SHOW_DATE

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        if (datePickerTarget == "start") startDate = it else targetDate = it
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formatDate(startDate),
                        onValueChange = {},
                        label = { Text("Start Date") },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                datePickerTarget = "start"
                                datePickerState.selectedDateMillis = startDate
                                showDatePicker = true
                            },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = formatDate(targetDate),
                        onValueChange = {},
                        label = { Text("Target Date") },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                datePickerTarget = "target"
                                datePickerState.selectedDateMillis = startDate
                                showDatePicker = true
                            },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        dismissButton = {
            if (isEditing) {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(onClick = { onSave(title, description, startDate, targetDate) }) {
                    Text("Save")
                }
            }
        }
    )
}