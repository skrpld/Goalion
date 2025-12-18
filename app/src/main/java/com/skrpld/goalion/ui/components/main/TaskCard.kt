package com.skrpld.goalion.ui.components.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.data.models.Task

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    task: Task,
    isEditing: Boolean,
    isSelected: Boolean,
    onTitleChange: (String) -> Unit,
    onEditDone: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            if (isEditing) {
                BasicTextField(
                    value = task.title,
                    onValueChange = onTitleChange,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onEditDone() }),
                    modifier = Modifier.focusRequester(focusRequester).fillMaxWidth()
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            } else {
                Text(
                    text = task.title.ifEmpty { "New Task" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else null,
                        color = if (task.status == TaskStatus.DONE) Color.Gray else Color.Unspecified
                    )
                )
            }
        }
    }
}