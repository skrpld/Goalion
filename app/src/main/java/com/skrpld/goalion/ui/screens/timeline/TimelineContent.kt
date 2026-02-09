package com.skrpld.goalion.ui.screens.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineContent(
    goals: List<TimelineGoalItem>,
    scrollOffsetX: Float,
    scrollOffsetY: Float,
    onScroll: (Float, Orientation) -> Unit,
    onToggleExpand: (Int) -> Unit
) {
    val density = LocalDensity.current
    val dayWidthPx = with(density) { DAY_WIDTH.toPx() }
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val itemProvider = remember(goals) {
        object : LazyLayoutItemProvider {
            override val itemCount: Int = goals.size
            @Composable override fun Item(index: Int, key: Any) {
                val item = goals[index]
                val durationMs = item.data.goal.targetDate - item.data.goal.startDate
                val daysDuration = durationMs.toFloat() / MILLIS_IN_DAY
                val widthDp = max(daysDuration * DAY_WIDTH.value, 40f).dp

                TimelineItemCard(
                    item = item,
                    width = widthDp,
                    onClick = { onToggleExpand(index) }
                )
            }
        }
    }

    LazyLayout(
        itemProvider = { itemProvider },
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                val firstVisibleDay = floor(scrollOffsetX / dayWidthPx).toInt()
                val daysToDraw = ceil(size.width / dayWidthPx).toInt() + 1

                for (i in 0..daysToDraw) {
                    val x = ((i + firstVisibleDay) * dayWidthPx) - scrollOffsetX
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
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
        val baseTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

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
            currentY += height + 16.dp.toPx().toInt()
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