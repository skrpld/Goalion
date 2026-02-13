package com.skrpld.goalion.presentation.screens.timeline

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.domain.model.Goal
import com.skrpld.goalion.domain.model.Task
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun TimelineHeader(scrollOffsetX: Float) {
    val density = LocalDensity.current
    val dayWidthPx = with(density) { DAY_WIDTH.toPx() }
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    val baseTime = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -10)
        }.timeInMillis
    }

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
        val widthPx = with(density) { maxWidth.toPx() }

        val firstVisibleDayIndex = floor(scrollOffsetX / dayWidthPx).toInt()
        val visibleDaysCount = ceil(widthPx / dayWidthPx).toInt() + 1

        for (i in 0..visibleDaysCount) {
            val dayIndex = i + firstVisibleDayIndex
            val x = (dayIndex * dayWidthPx) - scrollOffsetX

            val dateMillis = baseTime + (dayIndex * MILLIS_IN_DAY)

            val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
            val dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""
            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH).toString()

            val isToday = DateUtils.isToday(dateMillis)
            val textColor = if(isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

            Column(
                modifier = Modifier
                    .width(DAY_WIDTH)
                    .offset { IntOffset(x.roundToInt(), 0) }
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dayNumber,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = if(isToday) FontWeight.Bold else FontWeight.Normal),
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun TimelineContent(
    goals: List<TimelineGoalItem>,
    scrollOffsetX: Float,
    scrollOffsetY: Float,
    onScroll: (Float, Orientation) -> Unit,
    onToggleExpand: (Int) -> Unit,
    onEditGoal: (Goal) -> Unit,
    onAddTask: (String) -> Unit,
    onEditTask: (Task) -> Unit
) {
    val density = LocalDensity.current
    val dayWidthPx = with(density) { DAY_WIDTH.toPx() }
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val baseTime = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -10)
        }.timeInMillis
    }

    LazyLayout(
        itemProvider = {
            object : LazyLayoutItemProvider {
                override val itemCount: Int = goals.size
                @Composable override fun Item(index: Int, key: Any) {
                    val item = goals[index]
                    val durationMs = item.data.goal.targetDate - item.data.goal.startDate
                    val daysDuration = durationMs.toFloat() / MILLIS_IN_DAY
                    val widthDp = max(daysDuration * DAY_WIDTH.value, 50f).dp

                    TimelineGoalCard(
                        item = item,
                        width = widthDp,
                        onClick = { onToggleExpand(index) },
                        onEditGoal = { onEditGoal(item.data.goal) },
                        onAddTask = { onAddTask(item.data.goal.id) },
                        onEditTask = onEditTask
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                val firstVisibleDay = floor(scrollOffsetX / dayWidthPx).toInt()
                val daysToDraw = ceil(size.width / dayWidthPx).toInt() + 1

                for (i in 0..daysToDraw) {
                    val x = ((i + firstVisibleDay) * dayWidthPx) - scrollOffsetX
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height))
                }

                val now = System.currentTimeMillis()
                val daysFromBase = (now - baseTime).toFloat() / MILLIS_IN_DAY
                val currentDayX = (daysFromBase * dayWidthPx) - scrollOffsetX

                if (currentDayX in -10f..size.width + 10f) {
                    drawLine(
                        color = Color.Green,
                        start = Offset(currentDayX, 0f),
                        end = Offset(currentDayX, size.height),
                        strokeWidth = 3.dp.toPx()
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
    ) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val placeables = mutableListOf<Triple<Placeable, Int, Int>>()

        var currentY = 24.dp.toPx().toInt()

        for (i in goals.indices) {
            val item = goals[i]
            val startOffsetMs = item.data.goal.startDate - baseTime
            val xPosition = (startOffsetMs.toFloat() / MILLIS_IN_DAY * dayWidthPx).roundToInt()

            val placeable = compose(i).first().measure(Constraints())
            val height = placeable.height

            val isVisibleVertically = (currentY + height >= scrollOffsetY) &&
                    (currentY <= scrollOffsetY + layoutHeight)

            if (isVisibleVertically) {
                placeables.add(Triple(placeable, xPosition, currentY))
            }
            currentY += height + 12.dp.toPx().toInt()
        }

        layout(layoutWidth, layoutHeight) {
            placeables.forEach { (placeable, xPos, yPos) ->
                placeable.place(
                    x = xPos - scrollOffsetX.roundToInt(),
                    y = yPos - scrollOffsetY.roundToInt()
                )
            }
        }
    }
}