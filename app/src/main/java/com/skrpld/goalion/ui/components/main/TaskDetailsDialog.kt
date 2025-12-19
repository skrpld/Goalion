package com.skrpld.goalion.ui.components.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.skrpld.goalion.data.models.Task

@Composable
fun TaskDetailsDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSaveDescription: (String) -> Unit
) {
    // Используем локальное состояние, чтобы текст печатался плавно
    var localDescription by remember(task.id) { mutableStateOf(task.description) }

    Dialog(onDismissRequest = {
        onSaveDescription(localDescription)
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = task.title.ifEmpty { "No Title" },
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = localDescription,
                    onValueChange = { localDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        onSaveDescription(localDescription)
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save & Close")
                }
            }
        }
    }
}