package com.skrpld.goalion.ui.components.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.skrpld.goalion.data.models.Task

@Composable
fun TaskRow(
    task: Task,
    startEditing: Boolean,
    onEditStarted: () -> Unit,
    onSave: (String) -> Unit,
    onStatusClick: () -> Unit,
    onPriorityClick: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var hasGainedFocus by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var tempTitle by remember { mutableStateOf(task.title) }

    val density = LocalDensity.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0

    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen && isEditing) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(startEditing) {
        if (startEditing) {
            isEditing = true
            hasGainedFocus = false
            onEditStarted()
        }
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    val (statusIcon, statusTint) = when (task.status) {
        TaskStatus.TODO -> Icons.Default.RadioButtonUnchecked to MaterialTheme.colorScheme.onSurfaceVariant
        TaskStatus.DOING -> Icons.Default.Circle to MaterialTheme.colorScheme.primary
        TaskStatus.DONE -> Icons.Default.CheckCircle to Color.Gray
    }

    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> Color.Red
        TaskPriority.NORMAL -> Color.Green
        TaskPriority.LOW -> Color.Gray
    }

    val titleTextStyle = if (task.status == TaskStatus.DONE) {
        MaterialTheme.typography.bodyMedium.copy(
            textDecoration = TextDecoration.LineThrough,
            color = Color.Gray
        )
    } else {
        MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        IconButton(
            onClick = onStatusClick,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = "Status",
                tint = statusTint
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (isEditing) {
            BasicTextField(
                value = tempTitle,
                onValueChange = { tempTitle = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            hasGainedFocus = true
                        }
                        if (!focusState.isFocused && hasGainedFocus) {
                            onSave(tempTitle)
                            isEditing = false
                            hasGainedFocus = false
                        }
                    },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                })
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        isEditing = true
                        hasGainedFocus = false
                    }
            ) {
                Text(
                    text = task.title.ifEmpty { "New Task" },
                    style = if (task.title.isEmpty()) MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline) else titleTextStyle
                )

                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = priorityColor),
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable { onPriorityClick() }
                ) {
                    Text(
                        text = task.priority.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = {
                    isEditing = true
                    hasGainedFocus = false
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Task",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}