package com.skrpld.goalion.ui.screens.timeline

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrpld.goalion.domain.entities.GoalWithTasks
import com.skrpld.goalion.domain.usecases.GoalInteractors
import com.skrpld.goalion.domain.usecases.TaskInteractors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimelineUiState(
    val goals: List<TimelineGoalItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class TimelineGoalItem(
    val data: GoalWithTasks,
    val isExpanded: Boolean = false
)

class TimelineViewModel(
    savedStateHandle: SavedStateHandle,
    private val goalInteractors: GoalInteractors,
    private val taskInteractors: TaskInteractors
) : ViewModel() {

    private val profileId: String = savedStateHandle.get<String>("profileId") ?: "invalid_id"

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState = _uiState.asStateFlow()

    var scrollOffsetY by mutableFloatStateOf(0f)
        private set
    var scrollOffsetX by mutableFloatStateOf(0f)
        private set

    init {
        observeData()
    }

    private fun observeData() {
        _uiState.update { it.copy(isLoading = true) }

        goalInteractors.getWithTasks(profileId)
            .onEach { dbData ->
                val expandedIds = _uiState.value.goals
                    .filter { it.isExpanded }
                    .map { it.data.goal.id }
                    .toSet()

                val items = dbData.map { goalWithTasks ->
                    TimelineGoalItem(
                        data = goalWithTasks,
                        isExpanded = goalWithTasks.goal.id in expandedIds
                    )
                }
                _uiState.update { it.copy(goals = items, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun getGoalById(goalId: String) = _uiState.value.goals.find { it.data.goal.id == goalId }?.data?.goal
    private fun getTaskById(taskId: String) = _uiState.value.goals
        .flatMap { it.data.tasks }
        .find { it.id == taskId }

// --- Domain: Goals ---

    fun manageGoal(
        id: String? = null,
        title: String? = null,
        status: Boolean? = null,
        isDelete: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                if (isDelete && id != null) {
                    goalInteractors.delete(id)
                    return@launch
                }

                if (id == null) {
                    goalInteractors.update(
                        id = java.util.UUID.randomUUID().toString(),
                        profileId = profileId,
                        title = title ?: "New Goal",
                        description = "", status = false, priority = 0, order = 0,
                        startDate = System.currentTimeMillis(), targetDate = 0
                    )
                } else {
                    val goal = getGoalById(id) ?: return@launch
                    goalInteractors.update(
                        id = goal.id,
                        profileId = goal.profileId,
                        title = title ?: goal.title,
                        description = goal.description,
                        status = status ?: goal.status,
                        priority = goal.priority,
                        order = goal.order,
                        startDate = goal.startDate,
                        targetDate = goal.targetDate
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch { goalInteractors.delete(goalId) }
    }

// --- Domain: Tasks ---

    fun manageTask(
        id: String? = null,
        goalId: String? = null,
        title: String? = null,
        status: Boolean? = null,
        isDelete: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                if (isDelete && id != null) {
                    taskInteractors.delete(id)
                    return@launch
                }

                if (id == null && goalId != null) {
                    taskInteractors.update(
                        id = java.util.UUID.randomUUID().toString(),
                        goalId = goalId,
                        title = title ?: "New Task",
                        description = "", status = false, priority = 0, order = 0,
                        startDate = System.currentTimeMillis(), targetDate = 0
                    )
                    val index = _uiState.value.goals.indexOfFirst { it.data.goal.id == goalId }
                    if (index != -1 && !_uiState.value.goals[index].isExpanded) toggleGoalExpansion(index)
                } else if (id != null) {
                    val task = getTaskById(id) ?: return@launch
                    taskInteractors.update(
                        id = task.id,
                        goalId = task.goalId,
                        title = title ?: task.title,
                        description = task.description,
                        status = status ?: task.status,
                        priority = task.priority,
                        order = task.order,
                        startDate = task.startDate,
                        targetDate = task.targetDate
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch { taskInteractors.delete(taskId) }
    }

// --- UI Logic ---

    fun toggleGoalExpansion(index: Int) {
        _uiState.update { state ->
            val newList = state.goals.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(isExpanded = !newList[index].isExpanded)
            }
            state.copy(goals = newList)
        }
    }

    fun onScroll(delta: Float, orientation: Orientation) {
        if (orientation == Orientation.Vertical) {
            scrollOffsetY = (scrollOffsetY - delta).coerceAtLeast(0f)
        } else {
            scrollOffsetX -= delta
        }
    }
}