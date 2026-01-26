package com.skrpld.goalion.ui.components.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.ui.theme.HighPriorityColor
import com.skrpld.goalion.ui.theme.LowPriorityColor
import com.skrpld.goalion.ui.theme.NormalPriorityColor

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
    val isDone = task.status == TaskStatus.DONE
    val priorityColor = when(task.priority) {
        0 -> HighPriorityColor
        1 -> NormalPriorityColor
        else -> LowPriorityColor
    }

    GoalionCard(
        modifier = Modifier.alpha(if (isDone) 0.5f else 1f),
        isSelected = isSelected,
        onClick = onClick,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (!isDone) {
                        drawRect(color = priorityColor, size = size.copy(width = 6.dp.toPx()))
                    }
                }
                .padding(12.dp)
                .padding(start = if (isDone) 0.dp else 4.dp)
        ) {
            EditableTitle(
                title = task.title,
                isEditing = isEditing,
                isDone = isDone,
                onTitleChange = onTitleChange,
                onEditDone = onEditDone,
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = "Task title..."
            )
        }
    }
}