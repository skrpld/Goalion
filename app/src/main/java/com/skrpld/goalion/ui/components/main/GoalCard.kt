package com.skrpld.goalion.ui.components.main

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.ui.screens.main.MainViewModel
import com.skrpld.goalion.ui.theme.HighPriorityColor
import com.skrpld.goalion.ui.theme.LowPriorityColor
import com.skrpld.goalion.ui.theme.NormalPriorityColor

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
    modifier: Modifier = Modifier
) {
    val isGoalSelected = selectedTarget is MainViewModel.ActionTarget.GoalTarget && selectedTarget.goal.id == goal.id
    val isDone = goal.status == TaskStatus.DONE

    val priorityColor = when(goal.priority) {
        0 -> HighPriorityColor
        1 -> NormalPriorityColor
        else -> LowPriorityColor
    }

    GoalionCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isDone) 0.5f else 1f),
        isSelected = isGoalSelected,
        onClick = onToggle,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (!isDone) {
                        drawRect(color = priorityColor, size = size.copy(width = 6.dp.toPx()))
                    }
                }
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(start = if (isDone) 0.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditableTitle(
                    title = goal.title,
                    isEditing = isEditing,
                    isDone = isDone,
                    onTitleChange = onTitleChange,
                    onEditDone = onEditDone,
                    textStyle = MaterialTheme.typography.titleMedium,
                    placeholder = "Goal title..."
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp, bottom = 16.dp),
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

                    GoalionCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onAddSubTask
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add task", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}