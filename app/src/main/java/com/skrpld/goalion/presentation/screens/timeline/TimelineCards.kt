package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skrpld.goalion.domain.model.Task
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineGoalCard(
    item: TimelineGoalItem,
    initialWidth: Dp,
    totalWidthPx: Float,
    absoluteX: Float,
    scrollOffsetX: Float,
    screenWidthPx: Float,
    onClick: () -> Unit,
    onEditGoal: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    onToggleStatus: () -> Unit,
    onToggleTaskStatus: (Task) -> Unit,
    onViewTask: (Task) -> Unit,
    onJump: (Float) -> Unit
) {
    val viewState by remember(absoluteX, totalWidthPx, screenWidthPx, scrollOffsetX) {
        derivedStateOf {
            val screenStart = scrollOffsetX
            val screenEnd = scrollOffsetX + screenWidthPx
            val itemEnd = absoluteX + totalWidthPx

            val visibleStart = max(absoluteX, screenStart)
            val visibleEnd = min(itemEnd, screenEnd)
            val visibleWidth = visibleEnd - visibleStart

            if (visibleWidth > 0 && visibleWidth < 160f) {
                val stickToLeft = absoluteX < screenStart
                CardViewState.Marker(stickToLeft)
            } else {
                CardViewState.Full
            }
        }
    }

    val priorityColor = Priority.fromInt(item.data.goal.priority).color
    val density = LocalDensity.current

    when (val state = viewState) {
        is CardViewState.Marker -> {
            Box(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)

                        val parentX = absoluteX - scrollOffsetX
                        val offsetX = if (state.stickToLeft) {
                            (scrollOffsetX - absoluteX).coerceAtLeast(0f)
                        } else {
                            ((scrollOffsetX + screenWidthPx) - absoluteX - placeable.width).coerceAtMost(totalWidthPx - placeable.width)
                        }

                        layout(placeable.width, placeable.height) {
                            placeable.place(offsetX.roundToInt(), 0)
                        }
                    }
                    .width(40.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(priorityColor)
                    .clickable {
                        if (state.stickToLeft) onJump(absoluteX) else onJump(absoluteX + totalWidthPx)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if(state.stickToLeft) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                    contentDescription = "Jump",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        CardViewState.Full -> {

            var showMenu by remember { mutableStateOf(false) }
            val containerColor = if(item.data.goal.status) Color.Gray.copy(alpha=0.2f) else MaterialTheme.colorScheme.primaryContainer
            val contentColor = if(item.data.goal.status) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer

            Layout(
                content = {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerColor)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = onClick,
                                onLongClick = { showMenu = true }
                            )
                            .drawBehind {
                                drawRect(priorityColor, Offset.Zero, Size(10.dp.toPx(), size.height))
                            }
                            .padding(start = 14.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
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
                                TimelineTaskItem(task, { onEditTask(task) }, { onToggleTaskStatus(task) }, { onViewTask(task) })
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            ) { measurables, constraints ->
                val parentX = absoluteX - scrollOffsetX
                val screenStartLocal = -parentX

                val visStart = max(0f, screenStartLocal)
                val visEnd = min(totalWidthPx, screenStartLocal + screenWidthPx)
                val visWidth = (visEnd - visStart).coerceAtLeast(0f)

                val childConstraints = constraints.copy(
                    minWidth = 0,
                    maxWidth = visWidth.roundToInt().coerceAtLeast(100)
                )
                val placeable = measurables[0].measure(childConstraints)

                layout(totalWidthPx.roundToInt(), placeable.height) {
                    placeable.place(visStart.roundToInt(), 0)
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
                .combinedClickable(onClick = onViewTask, onLongClick = { showMenu = true })
                .height(IntrinsicSize.Min)
        ) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(priorityColor))

            Column(modifier = Modifier.padding(6.dp).weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    textDecoration = textDecoration,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text("Toggle Status") }, onClick = { showMenu = false; onToggleStatus() })
            DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEditTask() })
        }
    }
}