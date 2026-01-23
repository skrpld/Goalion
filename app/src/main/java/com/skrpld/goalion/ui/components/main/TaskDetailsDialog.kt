package com.skrpld.goalion.ui.components.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun TaskDetailsDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSaveDescription: (String) -> Unit
) {
    var localDescription by remember(task.id) { mutableStateOf(task.description) }

    Dialog(onDismissRequest = {
        onSaveDescription(localDescription)
        onDismiss()
    }) {
        GoalionCard(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            isSelected = false
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = task.title.ifEmpty { "No Title" },
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = localDescription,
                    onValueChange = { localDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(Modifier.height(16.dp))
                GoalionCard(
                    onClick = {
                        onSaveDescription(localDescription)
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save & Close", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
    }
}