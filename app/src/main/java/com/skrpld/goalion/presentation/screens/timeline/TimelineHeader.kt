package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun TimelineHeader(scrollOffsetX: Float) {
    val density = LocalDensity.current
    val dayWidthPx = with(density) { DAY_WIDTH.toPx() }
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
        val widthPx = with(density) { maxWidth.toPx() }
        val calendar = remember { Calendar.getInstance() }

        val firstVisibleDayIndex = floor(scrollOffsetX / dayWidthPx).toInt()
        val visibleDaysCount = ceil(widthPx / dayWidthPx).toInt() + 2

        for (i in -1..visibleDaysCount) {
            val dayOffset = i + firstVisibleDayIndex
            val x = (dayOffset * dayWidthPx) - scrollOffsetX

            val dateMillis = remember(dayOffset) {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.DAY_OF_YEAR, dayOffset)
                }.timeInMillis
            }

            calendar.timeInMillis = dateMillis
            val dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""
            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH).toString()

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
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}