package com.skrpld.goalion.presentation.screens.timeline

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

@OptIn(ExperimentalFoundationApi::class)
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
    onViewTask: (Task) -> Unit,
    onToggleGoalStatus: (Goal) -> Unit,
    onToggleTaskStatus: (Task) -> Unit,
    onJump: (Float) -> Unit
) {
    val density = LocalDensity.current
    val dayWidthPx = with(density) { dayWidth.toPx() }
    val itemHeightPx = with(density) { 70.dp.toPx() }
    val spacingPx = with(density) { 8.dp.toPx() }

    LaunchedEffect(goals.size) {
        val totalH = goals.size * (itemHeightPx + spacingPx)
        onContentHeightChanged(totalH)
    }

    val itemProvider = remember(goals, scrollOffsetX, screenWidthPx) {
        object : LazyLayoutItemProvider {
            override val itemCount: Int = goals.size

            @Composable
            override fun Item(index: Int, key: Any) {
                val item = goals[index]
                val startX = ((item.data.goal.startDate - baseTime) / MILLIS_IN_DAY.toFloat()) * dayWidthPx
                val endX = ((item.data.goal.targetDate - baseTime) / MILLIS_IN_DAY.toFloat()) * dayWidthPx
                val widthPx = maxOf(endX - startX, with(density) { 40.dp.toPx() })

                TimelineGoalCard(
                    item = item,
                    initialWidth = with(density) { widthPx.toDp() },
                    totalWidthPx = widthPx,
                    absoluteX = startX,
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
    }

    LazyLayout(
        itemProvider = { itemProvider },
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onScroll(dragAmount.x, Orientation.Horizontal)
                    onScroll(dragAmount.y, Orientation.Vertical)
                }
            }
    ) { constraints ->
        val placeables = mutableListOf<Pair<Placeable, IntOffset>>()
        var currentY = -scrollOffsetY

        for (i in 0 until itemProvider.itemCount) {
            val item = goals[i]
            val startPx = ((item.data.goal.startDate - baseTime) / MILLIS_IN_DAY.toFloat()) * dayWidthPx
            val endPx = ((item.data.goal.targetDate - baseTime) / MILLIS_IN_DAY.toFloat()) * dayWidthPx
            val widthPx = maxOf(endPx - startPx, density.run { 40.dp.toPx() })

            if (currentY + itemHeightPx > 0 && currentY < constraints.maxHeight) {
                val measurables = measure(i, Constraints.fixedWidth(widthPx.roundToInt()))
                val placeable = measurables.firstOrNull()

                if (placeable != null) {
                    val screenX = (startPx - scrollOffsetX).roundToInt()
                    placeables.add(placeable to IntOffset(screenX, currentY.roundToInt()))
                    currentY += placeable.height + spacingPx
                } else {
                    currentY += itemHeightPx + spacingPx
                }
            } else {
                currentY += itemHeightPx + spacingPx
            }
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { (placeable, offset) ->
                placeable.placeRelative(offset)
            }
        }
    }
}