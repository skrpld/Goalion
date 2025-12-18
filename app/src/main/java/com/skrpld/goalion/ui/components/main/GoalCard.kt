package com.skrpld.goalion.ui.components.main

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.data.models.Goal
import com.skrpld.goalion.data.models.Task

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalCard(
    goal: Goal,
    tasks: List<Task>,
    isExpanded: Boolean,
    isEditing: Boolean,
    isSelected: Boolean,
    editingTaskId: Int?,
    selectedTaskId: Int?,
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
    val focusRequester = remember { FocusRequester() }
    val borderColor = when(goal.priority) {
        0 -> Color.Red
        1 -> Color.Yellow
        else -> Color.Green
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.fillMaxHeight().width(6.dp).background(borderColor))

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
                    Column(modifier = Modifier.weight(1f)) {
                        if (isEditing) {
                            BasicTextField(
                                value = goal.title,
                                onValueChange = onTitleChange,
                                textStyle = MaterialTheme.typography.titleMedium,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { onEditDone() }),
                                modifier = Modifier.focusRequester(focusRequester)
                            )
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        } else {
                            Text(
                                text = goal.title.ifEmpty { "New Goal" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    textDecoration = if (goal.status == TaskStatus.DONE) TextDecoration.LineThrough else null,
                                    color = if (goal.status == TaskStatus.DONE) Color.Gray else Color.Unspecified
                                )
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tasks.forEach { task ->
                            TaskCard(
                                task = task,
                                isEditing = editingTaskId == task.id,
                                isSelected = selectedTaskId == task.id,
                                onTitleChange = { onTaskTitleChange(task.id, it) },
                                onEditDone = onEditDone,
                                onClick = { onTaskClick(task) },
                                onLongClick = { onTaskLongClick(task) },
                                onDoubleClick = { onTaskDoubleClick(task) }
                            )
                        }

                        OutlinedCard(
                            onClick = onAddSubTask,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Add task", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}