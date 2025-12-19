package com.skrpld.goalion.ui.components.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
    val priorityColor = when(task.priority) {
        0 -> MaterialTheme.colorScheme.error
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.medium
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .drawBehind {
                    drawRect(
                        color = priorityColor,
                        size = size.copy(width = 6.dp.toPx())
                    )
                }
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                EditableTitle(
                    title = task.title,
                    isEditing = isEditing,
                    isDone = task.status == TaskStatus.DONE,
                    onTitleChange = onTitleChange,
                    onEditDone = onEditDone,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    placeholder = "NewTask"
                )
            }
        }
    }
}