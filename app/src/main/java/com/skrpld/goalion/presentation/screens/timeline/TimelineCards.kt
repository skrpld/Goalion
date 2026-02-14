package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skrpld.goalion.domain.model.Task
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineGoalCard(
    item: TimelineGoalItem,
    width: Dp,
    scrollOffsetX: Float,
    absoluteX: Float,
    onClick: () -> Unit,
    onEditGoal: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    onToggleStatus: () -> Unit,
    onToggleTaskStatus: (Task) -> Unit,
    onViewTask: (Task) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val priorityColor = Priority.fromInt(item.data.goal.priority).color
    val density = LocalDensity.current

    val containerColor = if(item.data.goal.status) Color.Gray.copy(alpha=0.2f) else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if(item.data.goal.status) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer

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
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .drawBehind {
                    drawRect(
                        color = priorityColor,
                        topLeft = Offset.Zero,
                        size = Size(10.dp.toPx(), size.height)
                    )
                }
                .padding(start = 14.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val boxWidthPx = with(density) { maxWidth.toPx() }

                val offsetFromScreenLeft = scrollOffsetX - absoluteX

                val maxShift = (boxWidthPx - with(density){ 80.dp.toPx() }).coerceAtLeast(0f)
                val stickyOffset = offsetFromScreenLeft.coerceIn(0f, maxShift)

                Text(
                    text = item.data.goal.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.offset { IntOffset(stickyOffset.roundToInt(), 0) }
                )
            }

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
                        onEditTask = { onEditTask(task) },
                        onToggleStatus = { onToggleTaskStatus(task) },
                        onViewTask = { onViewTask(task) }
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
                text = { Text(if (item.data.goal.status) "Uncomplete" else "Complete") },
                onClick = { showMenu = false; onToggleStatus() },
                leadingIcon = { Icon(Icons.Default.CheckCircle, null) }
            )
            DropdownMenuItem(
                text = { Text("Add Task") },
                onClick = { showMenu = false; onAddTask() },
                leadingIcon = { Icon(Icons.Default.Add, null) }
            )
            DropdownMenuItem(
                text = { Text("Edit Goal") },
                onClick = { showMenu = false; onEditGoal() },
                leadingIcon = { Icon(Icons.Default.Edit, null) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineTaskItem(
    task: Task,
    onEditTask: () -> Unit,
    onToggleStatus: () -> Unit,
    onViewTask: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val priorityColor = Priority.fromInt(task.priority).color
    val isCompleted = task.status

    val textColor = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
    val textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if(isCompleted) 0.5f else 1f)

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(6.dp))
                .clip(RoundedCornerShape(6.dp))
                .combinedClickable(
                    onClick = onViewTask,
                    onLongClick = { showMenu = true }
                )
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(priorityColor)
            )

            Column(modifier = Modifier.padding(6.dp).weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    textDecoration = textDecoration,
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
                text = { Text(if (isCompleted) "Uncomplete" else "Complete") },
                onClick = { showMenu = false; onToggleStatus() },
                leadingIcon = { Icon(Icons.Default.Check, null) }
            )
            DropdownMenuItem(
                text = { Text("Edit Task") },
                onClick = { showMenu = false; onEditTask() },
                leadingIcon = { Icon(Icons.Default.Edit, null) }
            )
        }
    }
}