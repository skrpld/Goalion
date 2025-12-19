package com.skrpld.goalion.ui.components.main

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier
) {
    val isGoalSelected = selectedTarget is MainViewModel.ActionTarget.GoalTarget && selectedTarget.goal.id == goal.id

    val priorityColor = when(goal.priority) {
        0 -> MaterialTheme.colorScheme.error
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(
                width = if (isGoalSelected) 2.dp else 0.dp,
                color = if (isGoalSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        IntrinsicRow {
            Box(modifier = Modifier.fillMaxHeight().width(6.dp).background(priorityColor))

            Column {
                Row(
                    modifier = Modifier
                        .combinedClickable(
                            onClick = onToggle,
                            onLongClick = onLongClick,
                            onDoubleClick = onDoubleClick
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditableTitle(
                        title = goal.title,
                        isEditing = isEditing,
                        isDone = goal.status == TaskStatus.DONE,
                        onTitleChange = onTitleChange,
                        onEditDone = onEditDone,
                        textStyle = MaterialTheme.typography.titleMedium,
                        placeholder = "Новая цель"
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(start = 12.dp, end = 16.dp, bottom = 16.dp),
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
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Добавить задачу", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntrinsicRow(content: @Composable () -> Unit) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) { content() }
}