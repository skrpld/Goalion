package com.skrpld.goalion.presentation.screens.timeline

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.domain.model.Goal
import com.skrpld.goalion.domain.model.Task
import com.skrpld.goalion.presentation.screens.timeline.TimelineDateUtils.getBaseTime
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun TimelineHeader(
    scrollOffsetX: Float,
    zoomLevel: ZoomLevel,
    dayWidth: Dp
) {
    val density = LocalDensity.current
    val dayWidthPx = with(density) { dayWidth.toPx() }
    val baseTime = getBaseTime()
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .clipToBounds()
    ) {
        // --- Layer 1 ---

        val screenWidthPx = with(density) { maxWidth.toPx() }
        val firstVisibleIndex = floor(scrollOffsetX / dayWidthPx).toInt()
        val visibleCount = ceil(screenWidthPx / dayWidthPx).toInt() + 2

        for (i in 0 until visibleCount) {
            val index = firstVisibleIndex + i
            val x = (index * dayWidthPx) - scrollOffsetX
            val dateMillis = baseTime + (index * MILLIS_IN_DAY)

            val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
            val isToday = DateUtils.isToday(dateMillis)
            val isMonday = dayOfWeek == Calendar.MONDAY

            // Цвета
            val numberColor = when {
                isToday -> MaterialTheme.colorScheme.primary
                isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                else -> MaterialTheme.colorScheme.onSurface
            }

            val dividerColor = if (isMonday) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            val dividerWidth = if (isMonday) 2.dp else 1.dp

            Spacer(
                modifier = Modifier
                    .offset { IntOffset(x.roundToInt(), 0) }
                    .width(dividerWidth)
                    .fillMaxHeight()
                    .padding(top = 20.dp)
                    .background(dividerColor)
            )

            Column(
                modifier = Modifier
                    .width(dayWidth)
                    .offset { IntOffset(x.roundToInt(), 0) }
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (zoomLevel == ZoomLevel.DETAILED) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("00", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("12", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                } else {
                    Text(
                        text = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = numberColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        // --- Layer 2 ---

        val startMillis = baseTime + (firstVisibleIndex * MILLIS_IN_DAY)
        val endMillis = startMillis + (visibleCount * MILLIS_IN_DAY)

        val iteratorCal = Calendar.getInstance().apply { timeInMillis = startMillis }

        if (zoomLevel == ZoomLevel.NORMAL) {
            iteratorCal.set(Calendar.DAY_OF_MONTH, 1)

            while (iteratorCal.timeInMillis < endMillis) {
                val currentMonthStart = iteratorCal.timeInMillis
                val monthName = iteratorCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
                val year = iteratorCal.get(Calendar.YEAR)
                val headerText = "$monthName $year"

                iteratorCal.add(Calendar.MONTH, 1)
                val nextMonthStart = iteratorCal.timeInMillis

                val daysFromBase = (currentMonthStart - baseTime).toFloat() / MILLIS_IN_DAY
                val startX = (daysFromBase * dayWidthPx) - scrollOffsetX

                val daysInMonth = (nextMonthStart - currentMonthStart).toFloat() / MILLIS_IN_DAY
                val width = daysInMonth * dayWidthPx
                val endX = startX + width

                if (endX > 0 && startX < screenWidthPx) {
                    val visibleX = max(0f, startX).coerceAtMost(endX - 100f)

                    Text(
                        text = headerText.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .offset { IntOffset(visibleX.roundToInt(), 0) }
                            .padding(top = 4.dp, start = 8.dp)
                    )
                }
            }
        } else {
            for (i in 0 until visibleCount) {
                val index = firstVisibleIndex + i
                val dayStartMillis = baseTime + (index * MILLIS_IN_DAY)
                val startX = (index * dayWidthPx) - scrollOffsetX
                val endX = startX + dayWidthPx

                if (startX < screenWidthPx && endX > 0) {
                    val cal = Calendar.getInstance().apply { timeInMillis = dayStartMillis }
                    val dayStr = "${cal.get(Calendar.DAY_OF_MONTH)} ${cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())}"

                    Text(
                        text = dayStr,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .offset { IntOffset(startX.roundToInt(), 0) }
                            .width(dayWidth)
                            .padding(top = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineContent(
    goals: List<TimelineGoalItem>,
    scrollOffsetX: Float,
    scrollOffsetY: Float,
    zoomLevel: ZoomLevel,
    dayWidth: Dp,
    onScroll: (Float, Orientation) -> Unit,
    onToggleExpand: (Int) -> Unit,
    onEditGoal: (Goal) -> Unit,
    onAddTask: (String) -> Unit,
    onEditTask: (Task) -> Unit,
    onToggleGoalStatus: (Goal) -> Unit,
    onToggleTaskStatus: (Task) -> Unit,
    onViewTask: (Task) -> Unit
) {
    val density = LocalDensity.current
    val dayWidthPx = with(density) { dayWidth.toPx() }
    val baseTime = getBaseTime()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .drawBehind {
                val firstVisibleDay = floor(scrollOffsetX / dayWidthPx).toInt()
                val visibleDays = ceil(size.width / dayWidthPx).toInt() + 1

                for (i in 0..visibleDays) {
                    val x = ((firstVisibleDay + i) * dayWidthPx) - scrollOffsetX
                    drawLine(
                        Color.Gray.copy(alpha = 0.1f),
                        Offset(x, 0f),
                        Offset(x, size.height)
                    )
                }

                val now = System.currentTimeMillis()
                val daysFromBase = (now - baseTime).toFloat() / MILLIS_IN_DAY
                val currentX = (daysFromBase * dayWidthPx) - scrollOffsetX

                if (currentX in 0f..size.width) {
                    drawLine(
                        color = Color.Green,
                        start = Offset(currentX, 0f),
                        end = Offset(currentX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
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

                    val startOffsetMs = item.data.goal.startDate - baseTime
                    val absX = (startOffsetMs.toFloat() / MILLIS_IN_DAY) * dayWidthPx

                    TimelineGoalCard(
                        item = item,
                        width = widthDp,
                        scrollOffsetX = scrollOffsetX,
                        absoluteX = absX,
                        onClick = { onToggleExpand(index) },
                        onEditGoal = { onEditGoal(item.data.goal) },
                        onAddTask = { onAddTask(item.data.goal.id) },
                        onEditTask = onEditTask,
                        onToggleStatus = { onToggleGoalStatus(item.data.goal) },
                        onToggleTaskStatus = onToggleTaskStatus,
                        onViewTask = onViewTask
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

                val height = placeable.height
                val minY = currentY - scrollOffsetY
                val maxY = minY + height

                if (maxY > 0 && minY < constraints.maxHeight) {
                    placeables.add(placeable)
                    positions.add(IntOffset(xPosition, currentY))
                }

                currentY += height + paddingY
            }

            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val pos = positions[index]
                    placeable.place(
                        x = pos.x - scrollOffsetX.roundToInt(),
                        y = pos.y - scrollOffsetY.roundToInt()
                    )
                }
            }
        }
    }
}