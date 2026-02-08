package com.skrpld.goalion.ui.screens.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.domain.entities.GoalWithTasks
import java.util.*
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
    val scope = rememberCoroutineScope()

    val baseTime = remember {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        c.timeInMillis
    }

    val itemProvider = remember(goals) {
        object : LazyLayoutItemProvider {
            override val itemCount: Int = goals.size

            @Composable
            override fun Item(index: Int, key: Any) {
                val item = goals[index]

                val durationMs = item.data.goal.targetDate - item.data.goal.startDate
                val daysDuration = durationMs.toFloat() / MILLIS_IN_DAY
                val widthDp = max(daysDuration * DAY_WIDTH.value, 20f).dp

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
            .background(Color.White)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    onScroll(delta, Orientation.Horizontal)
                }
            )
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    onScroll(delta, Orientation.Vertical)
                }
            )
    ) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val placeables = ArrayList<Triple<Placeable, Int, Int>>()
        var currentY = 0

        for (i in 0 until goals.size) {
            val item = goals[i]

            val startOffsetMs = item.data.goal.startDate - baseTime
            val startOffsetDays = startOffsetMs.toFloat() / MILLIS_IN_DAY
            val xPosition = (startOffsetDays * dayWidthPx).roundToInt()

            val placeableList = compose(i).map { it.measure(Constraints()) }
            val placeable = placeableList[0]
            val height = placeable.height

            val itemTop = currentY
            val itemBottom = currentY + height
            val isVisibleVertically = (itemBottom >= scrollOffsetY) && (itemTop <= scrollOffsetY + layoutHeight)

            // TODO: make horizontal visible optimization

            if (isVisibleVertically) {
                placeables.add(Triple(placeable, xPosition, itemTop))
            }

            currentY += height + 20

            if (currentY > scrollOffsetY + layoutHeight + 500) {
                break
            }
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