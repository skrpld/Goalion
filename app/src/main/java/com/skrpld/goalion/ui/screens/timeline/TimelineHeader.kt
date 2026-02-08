package com.skrpld.goalion.ui.screens.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun TimelineHeader(scrollOffsetX: Float) {
    val density = LocalDensity.current
    val dayWidthPx = with(density) { DAY_WIDTH.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.background)
            .clipToBounds()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startOffset = scrollOffsetX

            val firstVisibleDayIndex = floor(startOffset / dayWidthPx).toInt()
            val visibleDaysCount = ceil(size.width / dayWidthPx).toInt() + 2

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val baseTime = calendar.timeInMillis

            for (i in 0..visibleDaysCount) {
                val dayOffset = i + firstVisibleDayIndex
                val x = (dayOffset * dayWidthPx) - startOffset

                val dateMillis = baseTime + (dayOffset * MILLIS_IN_DAY)

                drawLine(
                    color = Color.Gray,
                    start = Offset(x, 30f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }
        }

        // TODO: Make normal date line
        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset { androidx.compose.ui.unit.IntOffset(-scrollOffsetX.roundToInt(), 0) }
        ) {
            // TODO: Make normal row scroll virtualization
            Text("Timeline Scroll Area", modifier = Modifier.padding(10.dp))
        }
    }
}