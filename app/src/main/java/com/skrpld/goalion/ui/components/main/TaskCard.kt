package com.skrpld.goalion.ui.components.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
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
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when(task.priority) {
        0 -> MaterialTheme.colorScheme.error
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.small
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    onDoubleClick = onDoubleClick
                )
                .drawBehind {
                    drawRect(color = priorityColor, size = size.copy(width = 4.dp.toPx()))
                }
                .padding(12.dp)
        ) {
            EditableTitle(
                title = task.title,
                isEditing = isEditing,
                isDone = task.status == TaskStatus.DONE,
                onTitleChange = onTitleChange,
                onEditDone = onEditDone,
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = "Task title..."
            )
        }
    }
}