package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skrpld.goalion.domain.model.Task

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineGoalCard(
    item: TimelineGoalItem,
    width: Dp,
    onClick: () -> Unit,
    onEditGoal: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .width(width)
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.3f), RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(8.dp)
        ) {
            Text(
                text = item.data.goal.title,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.data.goal.description.isNotEmpty()) {
                Text(
                    text = item.data.goal.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = contentColor.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.isExpanded && item.data.tasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                item.data.tasks.forEach { task ->
                    TimelineTaskItem(
                        task = task,
                        onEditTask = { onEditTask(task) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit Goal") },
                onClick = {
                    showMenu = false
                    onEditGoal()
                },
                leadingIcon = { Icon(Icons.Default.Edit, null) }
            )
            DropdownMenuItem(
                text = { Text("Add Task") },
                onClick = {
                    showMenu = false
                    onAddTask()
                },
                leadingIcon = { Icon(Icons.Default.Add, null) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineTaskItem(
    task: Task,
    onEditTask: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { TODO("Open details") },
                    onLongClick = { showMenu = true }
                )
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit Task") },
                onClick = {
                    showMenu = false
                    onEditTask()
                },
                leadingIcon = { Icon(Icons.Default.Edit, null) }
            )
        }
    }
}