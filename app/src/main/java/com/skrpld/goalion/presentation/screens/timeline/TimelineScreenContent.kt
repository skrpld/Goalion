package com.skrpld.goalion.presentation.screens.timeline

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skrpld.goalion.domain.model.Goal
import com.skrpld.goalion.domain.model.Task
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun TimelineHeader(
    scrollOffsetX: Float,
    zoomLevel: ZoomLevel,
    dayWidth: Dp,
    baseTime: Long
) {
    val density = LocalDensity.current
    val dayWidthPx = with(density) { dayWidth.toPx() }
    val textMeasurer = rememberTextMeasurer()

    // Цвета и стили
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    val labelStyle = MaterialTheme.typography.labelMedium
    val monthStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(surfaceColor)
            .drawBehind {
                val screenWidth = size.width
                val height = size.height
                val bottomLineY = height - 1.dp.toPx()

                drawLine(
                    color = outlineColor,
                    start = Offset(0f, bottomLineY),
                    end = Offset(screenWidth, bottomLineY),
                    strokeWidth = 1.dp.toPx()
                )

                val firstVisibleIndex = floor(scrollOffsetX / dayWidthPx).toInt()
                val visibleCount = ceil(screenWidth / dayWidthPx).toInt() + 2

                for (i in 0 until visibleCount) {
                    val index = firstVisibleIndex + i
                    val x = (index * dayWidthPx) - scrollOffsetX
                    val dateMillis = baseTime + (index * MILLIS_IN_DAY)

                    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                    val monthStart = dayOfMonth == 1
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    val isMonday = dayOfWeek == Calendar.MONDAY
                    val isToday = DateUtils.isToday(dateMillis)

                    if (zoomLevel == ZoomLevel.NORMAL) {
                        val isFatSeparator = monthStart
                        val separatorColor = if (isFatSeparator) onSurfaceColor else if (isMonday) outlineColor else outlineColor.copy(alpha = 0.3f)
                        val separatorWidth = if (isFatSeparator) 3.dp.toPx() else if (isMonday) 2.dp.toPx() else 1.dp.toPx()
                        val separatorHeightRatio = if (isFatSeparator) 0.0f else 0.5f // Месячный разделитель на всю высоту

                        drawLine(
                            color = separatorColor,
                            start = Offset(x, height * separatorHeightRatio),
                            end = Offset(x, height),
                            strokeWidth = separatorWidth
                        )

                        val textColor = if (isToday) Color.Green else if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) errorColor.copy(alpha=0.8f) else onSurfaceColor
                        val textLayoutResult = textMeasurer.measure(
                            text = dayOfMonth.toString(),
                            style = labelStyle.copy(color = textColor, fontWeight = if(isToday || monthStart) FontWeight.Bold else FontWeight.Normal)
                        )

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x + (dayWidthPx - textLayoutResult.size.width) / 2,
                                height - textLayoutResult.size.height - 6.dp.toPx()
                            )
                        )

                        if (monthStart) {
                            val monthName = "${calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} ${calendar.get(Calendar.YEAR)}".uppercase()

                            val nextMonth = Calendar.getInstance().apply { timeInMillis = dateMillis; add(Calendar.MONTH, 1) }
                            val daysInMonth = (nextMonth.timeInMillis - dateMillis) / MILLIS_IN_DAY
                            val monthWidthPx = daysInMonth * dayWidthPx

                            val textRes = textMeasurer.measure(monthName, monthStyle.copy(color = onSurfaceVariant))
                            val textWidth = textRes.size.width

                            val stickyX = max(x, 0f).coerceAtMost(x + monthWidthPx - textWidth - 16.dp.toPx())

                            if (x + monthWidthPx > 0 && x < screenWidth) {
                                drawText(
                                    textLayoutResult = textRes,
                                    topLeft = Offset(stickyX + 8.dp.toPx(), 8.dp.toPx())
                                )
                            }
                        }

                    } else {
                        drawLine(
                            color = onSurfaceColor,
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 3.dp.toPx()
                        )

                        val hourWidth = dayWidthPx / 24f
                        for (h in 1 until 24) {
                            val hx = x + (h * hourWidth)
                            if (hx > 0 && hx < screenWidth) {
                                val isNoon = h == 12
                                val tickH = if(isNoon) height * 0.4f else height * 0.15f
                                val color = if(isNoon) onSurfaceColor.copy(alpha=0.5f) else outlineColor.copy(alpha=0.5f)

                                drawLine(
                                    color = color,
                                    start = Offset(hx, height - tickH),
                                    end = Offset(hx, height),
                                    strokeWidth = if(isNoon) 2.dp.toPx() else 1.dp.toPx()
                                )

                                if (h % 6 == 0) {
                                    val timeText = "$h"
                                    val tRes = textMeasurer.measure(timeText, labelStyle.copy(fontSize = 10.sp, color = Color.Gray))
                                    drawText(tRes, topLeft = Offset(hx + 2.dp.toPx(), height - 20.dp.toPx()))
                                }
                            }
                        }

                        val dateText = "${calendar.get(Calendar.DAY_OF_MONTH)} ${calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())}"
                        val dRes = textMeasurer.measure(dateText, monthStyle.copy(color = primaryColor))
                        drawText(dRes, topLeft = Offset(x + 8.dp.toPx(), 8.dp.toPx()))
                    }
                }
            }
    )
}

@Composable
fun TimelineContent(
    goals: List<TimelineGoalItem>,
    scrollOffsetX: Float,
    scrollOffsetY: Float,
    zoomLevel: ZoomLevel,
    dayWidth: Dp,
    screenWidthPx: Float,
    baseTime: Long,
    onContentHeightChanged: (Float) -> Unit,
    onScroll: (Float, Orientation) -> Unit,
    onToggleExpand: (Int) -> Unit,
    onEditGoal: (Goal) -> Unit,
    onAddTask: (String) -> Unit,
    onEditTask: (Task) -> Unit,
    onToggleGoalStatus: (Goal) -> Unit,
    onToggleTaskStatus: (Task) -> Unit,
    onViewTask: (Task) -> Unit,
    onJump: (Float) -> Unit
) {
    val density = LocalDensity.current
    val dayWidthPx = with(density) { dayWidth.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .drawBehind {
                val screenWidth = size.width
                val screenHeight = size.height

                val firstDay = floor(scrollOffsetX / dayWidthPx).toInt()
                val count = ceil(screenWidth / dayWidthPx).toInt() + 1

                for (i in 0..count) {
                    val x = ((firstDay + i) * dayWidthPx) - scrollOffsetX
                    drawLine(
                        Color.Gray.copy(alpha = 0.1f),
                        Offset(x, 0f),
                        Offset(x, screenHeight)
                    )
                }

                val now = System.currentTimeMillis()
                val daysFromBase = (now - baseTime).toFloat() / MILLIS_IN_DAY
                val currentX = (daysFromBase * dayWidthPx) - scrollOffsetX
                if (currentX in -10f..screenWidth + 10f) {
                    drawLine(Color.Green, Offset(currentX, 0f), Offset(currentX, screenHeight), strokeWidth = 2.dp.toPx())
                }
            }
            .draggable(
                state = rememberDraggableState { delta -> onScroll(delta, Orientation.Horizontal) },
                orientation = Orientation.Horizontal
            )
            .draggable(
                state = rememberDraggableState { delta -> onScroll(delta, Orientation.Vertical) },
                orientation = Orientation.Vertical
            )
    ) {
        Layout(
            content = {
                goals.forEachIndexed { index, item ->
                    val durationMs = item.data.goal.targetDate - item.data.goal.startDate
                    val durationDays = max(1f, durationMs.toFloat() / MILLIS_IN_DAY)
                    val widthDp = (durationDays * dayWidth.value).dp.coerceAtLeast(50.dp)
                    val widthPx = with(density) { widthDp.toPx() }

                    val startOffsetMs = item.data.goal.startDate - baseTime
                    val absoluteX = (startOffsetMs.toFloat() / MILLIS_IN_DAY) * dayWidthPx

                    TimelineGoalCard(
                        item = item,
                        initialWidth = widthDp,
                        totalWidthPx = widthPx,
                        absoluteX = absoluteX,
                        scrollOffsetX = scrollOffsetX,
                        screenWidthPx = screenWidthPx,
                        onClick = { onToggleExpand(index) },
                        onEditGoal = { onEditGoal(item.data.goal) },
                        onAddTask = { onAddTask(item.data.goal.id) },
                        onEditTask = onEditTask,
                        onToggleStatus = { onToggleGoalStatus(item.data.goal) },
                        onToggleTaskStatus = onToggleTaskStatus,
                        onViewTask = onViewTask,
                        onJump = onJump
                    )
                }
            }
        ) { measurables, constraints ->
            val placeables = mutableListOf<Placeable>()
            val positions = mutableListOf<IntOffset>()

            var currentY = 24.dp.roundToPx()
            val paddingY = 12.dp.roundToPx()

            measurables.forEachIndexed { index, measurable ->
                val item = goals[index]
                val startOffsetMs = item.data.goal.startDate - baseTime
                val xPosition = (startOffsetMs.toFloat() / MILLIS_IN_DAY * dayWidthPx).roundToInt()

                val placeable = measurable.measure(Constraints())

                placeables.add(placeable)
                positions.add(IntOffset(xPosition, currentY))

                currentY += placeable.height + paddingY
            }

            onContentHeightChanged(currentY.toFloat())

            layout(constraints.maxWidth, constraints.maxHeight) {
                val scrollXInt = scrollOffsetX.roundToInt()
                val scrollYInt = scrollOffsetY.roundToInt()

                placeables.forEachIndexed { index, placeable ->
                    val pos = positions[index]
                    val finalY = pos.y - scrollYInt

                    if (finalY + placeable.height > 0 && finalY < constraints.maxHeight) {
                        placeable.place(
                            x = pos.x - scrollXInt,
                            y = finalY
                        )
                    }
                }
            }
        }
    }
}