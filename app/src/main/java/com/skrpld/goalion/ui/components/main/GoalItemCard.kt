package com.skrpld.goalion.ui.components.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skrpld.goalion.data.database.TaskPriority
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.data.models.GoalWithTasks
import com.skrpld.goalion.data.models.Task

@Composable
fun GoalItemCard(
    item: GoalWithTasks,
    startEditing: Boolean,
    taskIdToEdit: Int?,
    onAddTaskClick: () -> Unit,
    onEditGoalTitle: (String) -> Unit,
    onGoalEditStarted: () -> Unit,
    onEditTaskTitle: (Task, String) -> Unit,
    onTaskEditStarted: () -> Unit,
    onTaskStatusClick: (Task) -> Unit,
    onTaskPriorityClick: (Task) -> Unit,
    onGoalStatusClick: () -> Unit,
    onGoalPriorityClick: () -> Unit
) {
    val shouldExpand = taskIdToEdit != null && item.tasks.any { it.id == taskIdToEdit }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(shouldExpand) {
        if (shouldExpand) expanded = true
    }

    var isEditing by remember { mutableStateOf(false) }
    var hasGainedFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val density = LocalDensity.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0

    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen && isEditing) focusManager.clearFocus()
    }

    LaunchedEffect(startEditing) {
        if (startEditing) {
            isEditing = true
            hasGainedFocus = false
            onGoalEditStarted()
        }
    }

    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }

    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Arrow Rotation"
    )

    val (statusIcon, statusTint) = when (item.goal.status) {
        TaskStatus.TODO -> Icons.Default.RadioButtonUnchecked to MaterialTheme.colorScheme.onSurfaceVariant
        TaskStatus.DOING -> Icons.Default.Circle to MaterialTheme.colorScheme.primary
        TaskStatus.DONE -> Icons.Default.CheckCircle to Color.Gray
    }

    val priorityColor = when (item.goal.priority) {
        TaskPriority.HIGH -> Color.Red
        TaskPriority.NORMAL -> Color.Green
        TaskPriority.LOW -> Color.Gray
    }

    val titleTextStyle = if (item.goal.status == TaskStatus.DONE) {
        MaterialTheme.typography.titleMedium.copy(
            textDecoration = TextDecoration.LineThrough,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
    } else {
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    val sortedTasks = remember(item.tasks) {
        item.tasks.sortedWith(
            compareBy<Task> {
                when (it.status) {
                    TaskStatus.DOING -> 0
                    TaskStatus.TODO -> 1
                    TaskStatus.DONE -> 2
                }
            }.thenBy {
                when (it.priority) {
                    TaskPriority.HIGH -> 0
                    TaskPriority.NORMAL -> 1
                    TaskPriority.LOW -> 2
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onGoalStatusClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "Goal Status",
                        tint = statusTint
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (isEditing) {
                    var tempTitle by remember { mutableStateOf(item.goal.title) }
                    BasicTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) hasGainedFocus = true
                                if (!focusState.isFocused && hasGainedFocus) {
                                    onEditGoalTitle(tempTitle)
                                    isEditing = false
                                    hasGainedFocus = false
                                }
                            },
                        textStyle = titleTextStyle,
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.goal.title.ifEmpty { "Unnamed Goal" },
                            style = if (item.goal.title.isEmpty()) MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.outline) else titleTextStyle
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Card(
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = priorityColor),
                            modifier = Modifier
                                .wrapContentSize()
                                .clickable { onGoalPriorityClick() }
                        ) {
                            Text(
                                text = item.goal.priority.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isEditing) {
                        IconButton(
                            onClick = {
                                isEditing = true
                                hasGainedFocus = false
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (sortedTasks.isEmpty()) {
                        Text(
                            text = "No tasks yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        sortedTasks.forEach { task ->
                            TaskRow(
                                task = task,
                                startEditing = task.id == taskIdToEdit,
                                onEditStarted = onTaskEditStarted,
                                onSave = { newName -> onEditTaskTitle(task, newName) },
                                onStatusClick = { onTaskStatusClick(task) },
                                onPriorityClick = { onTaskPriorityClick(task) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onAddTaskClick() }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Task", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}