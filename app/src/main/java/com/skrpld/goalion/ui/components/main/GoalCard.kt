package com.skrpld.goalion.ui.components.main

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.data.models.Goal
import com.skrpld.goalion.data.models.Task
import com.skrpld.goalion.ui.screens.main.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalCard(
    goal: Goal,
    tasks: List<Task>,
    isExpanded: Boolean,
    isEditing: Boolean,
    selectedTarget: MainViewModel.ActionTarget?,
    editingTaskId: String?,
    onToggle: () -> Unit,
    onLongClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onTaskLongClick: (Task) -> Unit,
    onTaskDoubleClick: (Task) -> Unit,
    onAddSubTask: () -> Unit,
    onTitleChange: (String) -> Unit,
    onTaskTitleChange: (Int, String) -> Unit,
    onEditDone: () -> Unit,
    onMoveTask: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isGoalSelected = selectedTarget is MainViewModel.ActionTarget.GoalTarget && selectedTarget.goal.id == goal.id

    val priorityColor = when(goal.priority) {
        0 -> MaterialTheme.colorScheme.error
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val damping = 0.8f
    val stiffness = 30f

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = damping,
                    stiffness = stiffness
                )
            )
            .border(
                width = if (isGoalSelected) 2.dp else 0.dp,
                color = if (isGoalSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = priorityColor,
                        size = size.copy(width = 6.dp.toPx())
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onToggle,
                        onLongClick = onLongClick,
                        onDoubleClick = onDoubleClick
                    )
                    .padding(16.dp)
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditableTitle(
                    title = goal.title,
                    isEditing = isEditing,
                    isDone = goal.status == TaskStatus.DONE,
                    onTitleChange = onTitleChange,
                    onEditDone = onEditDone,
                    textStyle = MaterialTheme.typography.titleMedium,
                    placeholder = "Goal title..."
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(dampingRatio = damping, stiffness = stiffness)
                ) + fadeIn(
                    animationSpec = spring(dampingRatio = damping, stiffness = stiffness)
                ),
                exit = shrinkVertically(
                    animationSpec = spring(dampingRatio = damping, stiffness = stiffness)
                ) + fadeOut(
                    animationSpec = spring(dampingRatio = damping, stiffness = stiffness)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tasks.forEach { task ->
                        val isTaskSelected = selectedTarget is MainViewModel.ActionTarget.TaskTarget && selectedTarget.task.id == task.id
                        TaskCard(
                            task = task,
                            isEditing = editingTaskId == "task_${task.id}",
                            isSelected = isTaskSelected,
                            onTitleChange = { onTaskTitleChange(task.id, it) },
                            onEditDone = onEditDone,
                            onClick = { onTaskClick(task) },
                            onLongClick = { onTaskLongClick(task) },
                            onDoubleClick = { onTaskDoubleClick(task) }
                        )
                    }

                    OutlinedButton(
                        onClick = onAddSubTask,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add task")
                    }
                }
            }
        }
    }
}