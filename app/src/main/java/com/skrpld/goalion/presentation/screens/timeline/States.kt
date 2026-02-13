package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.ui.unit.dp
import com.skrpld.goalion.domain.model.GoalWithTasks

val DAY_WIDTH = 48.dp
const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L

enum class EditMode {
    CREATE_GOAL, EDIT_GOAL, CREATE_TASK, EDIT_TASK
}

data class DialogState(
    val isOpen: Boolean = false,
    val mode: EditMode = EditMode.CREATE_GOAL,
    val entityId: String? = null,
    val parentId: String? = null,
    val initialTitle: String = "",
    val initialDescription: String = "",
    val initialStartDate: Long = System.currentTimeMillis(),
    val initialTargetDate: Long = System.currentTimeMillis()
)

data class TimelineUiState(
    val goals: List<TimelineGoalItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val dialogState: DialogState = DialogState()
)

data class TimelineGoalItem(
    val data: GoalWithTasks,
    val isExpanded: Boolean = false
)