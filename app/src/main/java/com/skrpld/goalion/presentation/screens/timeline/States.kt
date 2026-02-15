package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.domain.model.GoalWithTasks
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L
val DAY_WIDTH_NORMAL = 48.dp
val DAY_WIDTH_ZOOMED = 300.dp

enum class ZoomLevel {
    NORMAL, DETAILED
}

sealed class CardViewState {
    object Full : CardViewState()
    data class Marker(val stickToLeft: Boolean) : CardViewState()
}

enum class Priority(val value: Int, val color: Color) {
    HIGH(0, Color(0xFFE53935)),
    NORMAL(1, Color(0xFFFFB300)),
    LOW(2, Color(0xFF43A047));

    companion object {
        fun fromInt(value: Int): Priority = entries.find { it.value == value } ?: NORMAL
    }
}

enum class EditMode {
    CREATE_GOAL, EDIT_GOAL, CREATE_TASK, EDIT_TASK, VIEW_TASK
}

data class DialogState(
    val isOpen: Boolean = false,
    val mode: EditMode = EditMode.CREATE_GOAL,
    val entityId: String? = null,
    val parentId: String? = null,
    val initialTitle: String = "",
    val initialDescription: String = "",
    val initialStartDate: Long = System.currentTimeMillis(),
    val initialTargetDate: Long = System.currentTimeMillis(),
    val initialPriority: Int = 1
)

data class TimelineUiState(
    val goals: List<TimelineGoalItem> = emptyList(),
    val showCompleted: Boolean = false,
    val zoomLevel: ZoomLevel = ZoomLevel.NORMAL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val dialogState: DialogState = DialogState(),
    val scrollRequest: Long? = null
)

data class TimelineGoalItem(
    val data: GoalWithTasks,
    val isExpanded: Boolean = false
)

object TimelineDateUtils {
    fun getBaseTime(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -10)
        }.timeInMillis
    }

    fun isToday(millis: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}