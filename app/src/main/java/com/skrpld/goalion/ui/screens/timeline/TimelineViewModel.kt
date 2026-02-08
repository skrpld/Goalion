package com.skrpld.goalion.ui.screens.timeline

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrpld.goalion.domain.entities.GoalWithTasks
import com.skrpld.goalion.domain.usecases.CreateGoalUseCase
import com.skrpld.goalion.domain.usecases.CreateTaskUseCase
import com.skrpld.goalion.domain.usecases.DeleteGoalUseCase
import com.skrpld.goalion.domain.usecases.DeleteTaskUseCase
import com.skrpld.goalion.domain.usecases.GetGoalsWithTasksUseCase
import com.skrpld.goalion.domain.usecases.SyncGoalUseCase
import com.skrpld.goalion.domain.usecases.SyncTaskUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalDescriptionUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalOrderUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalPriorityUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalStartDateUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalStatusUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalTargetDateUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalTitleUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskDescriptionUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskOrderUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskPriorityUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskStartDateUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskStatusUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskTargetDateUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskTitleUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

val DAY_WIDTH = 48.dp
const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L

data class TimelineGoalItem(
    val data: GoalWithTasks,
    val isExpanded: Boolean = false
)

class TimelineViewModel(
    // TODO: Add all UseCases realisations
    // --- Goal UseCases ---
    private val getGoalsWithTasksUseCase: GetGoalsWithTasksUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
    private val updateGoalStatusUseCase: UpdateGoalStatusUseCase,
    private val updateGoalPriorityUseCase: UpdateGoalPriorityUseCase,
    private val updateGoalOrderUseCase: UpdateGoalOrderUseCase,
    private val syncGoalUseCase: SyncGoalUseCase,
    private val updateGoalTitleUseCase: UpdateGoalTitleUseCase,
    private val updateGoalDescriptionUseCase: UpdateGoalDescriptionUseCase,
    private val updateGoalStartDateUseCase: UpdateGoalStartDateUseCase,
    private val updateGoalTargetDateUseCase: UpdateGoalTargetDateUseCase,

    // --- Task UseCases ---
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val updateTaskPriorityUseCase: UpdateTaskPriorityUseCase,
    private val updateTaskOrderUseCase: UpdateTaskOrderUseCase,
    private val syncTaskUseCase: SyncTaskUseCase,
    private val updateTaskTitleUseCase: UpdateTaskTitleUseCase,
    private val updateTaskDescriptionUseCase: UpdateTaskDescriptionUseCase,
    private val updateTaskStartDateUseCase: UpdateTaskStartDateUseCase,
    private val updateTaskTargetDateUseCase: UpdateTaskTargetDateUseCase
) : ViewModel() {
    private val _goals = mutableStateListOf<TimelineGoalItem>()
    val goals: List<TimelineGoalItem> = _goals

    var scrollOffsetY by mutableFloatStateOf(0f)
        private set
    var scrollOffsetX by mutableFloatStateOf(0f)
        private set

    // TODO: Get it from current context
    private val profileId = "user_default_id"

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            getGoalsWithTasksUseCase(profileId).collectLatest { dbData ->
                val expandedIds = _goals.filter { it.isExpanded }.map { it.data.goal.id }.toSet()

                _goals.clear()
                _goals.addAll(dbData.map { goalWithTasks ->
                    TimelineGoalItem(
                        data = goalWithTasks,
                        isExpanded = goalWithTasks.goal.id in expandedIds
                    )
                })
            }
        }
    }

    // --- UI: Scroll ---

    fun onScroll(delta: Float, orientation: Orientation) {
        if (orientation == Orientation.Vertical) {
            scrollOffsetY = (scrollOffsetY - delta).coerceAtLeast(0f)
        } else {
            scrollOffsetX -= delta
        }
    }

    fun toggleGoalExpansion(index: Int) {
        if (index in _goals.indices) {
            val item = _goals[index]
            _goals[index] = item.copy(isExpanded = !item.isExpanded)
        }
    }

    // --- Domain: Goals ---

    fun createNewGoal(title: String) {
        viewModelScope.launch {
            try {
                createGoalUseCase(profileId, title, "")
            } catch (e: Exception) {
                // TODO: Handle error (show snackbar)
            }
        }
    }

    fun onGoalTitleChanged(goalId: String, newTitle: String) {
        viewModelScope.launch {
            updateGoalTitleUseCase(goalId, newTitle)
        }
    }

    fun onGoalStatusChanged(goalId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            updateGoalStatusUseCase(goalId, isCompleted)
        }
    }

    fun onGoalDateChanged(goalId: String, newStart: Long?, newTarget: Long?) {
        viewModelScope.launch {
            if (newStart != null) updateGoalStartDateUseCase(goalId, newStart)
            if (newTarget != null) updateGoalTargetDateUseCase(goalId, newTarget)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            deleteGoalUseCase(goalId)
        }
    }

    // --- Domain: Tasks ---

    fun addTaskToGoal(goalId: String, title: String) {
        viewModelScope.launch {
            createTaskUseCase(goalId, title, "")
            val index = _goals.indexOfFirst { it.data.goal.id == goalId }
            if (index != -1 && !_goals[index].isExpanded) {
                toggleGoalExpansion(index)
            }
        }
    }

    fun onTaskStatusChanged(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            updateTaskStatusUseCase(taskId, isCompleted)
        }
    }

    fun onTaskTitleChanged(taskId: String, newTitle: String) {
        viewModelScope.launch {
            updateTaskTitleUseCase(taskId, newTitle)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }
}