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

    fun createNewGoal(title: String) {
        viewModelScope.launch {
            try {
                goalInteractors.create(profileId, title, "")
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun onGoalTitleChanged(goalId: String, newTitle: String) {
        val goal = getGoalById(goalId) ?: return
        viewModelScope.launch {
            goalInteractors.update(
                id = goal.id,
                profileId = goal.profileId,
                title = newTitle,
                description = goal.description
                // TODO: update other fields
            )
        }
    }

    fun onGoalStatusChanged(goalId: String, isCompleted: Boolean) {
        val goal = getGoalById(goalId) ?: return
        viewModelScope.launch {
            goalInteractors.update(goal.id, goal.profileId, goal.title, goal.description)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch { goalInteractors.delete(goalId) }
    }

// --- Domain: Tasks ---

    fun addTaskToGoal(goalId: String, title: String) {
        viewModelScope.launch {
            taskInteractors.create(goalId, title, "")
            val index = _uiState.value.goals.indexOfFirst { it.data.goal.id == goalId }
            if (index != -1 && !_uiState.value.goals[index].isExpanded) {
                toggleGoalExpansion(index)
            }
        }
    }

    fun onTaskTitleChanged(taskId: String, newTitle: String) {
        val task = getTaskById(taskId) ?: return
        viewModelScope.launch {
            taskInteractors.update(
                id = task.id,
                goalId = task.goalId,
                title = newTitle,
                description = task.description
            )
        }
    }

    fun onTaskStatusChanged(taskId: String, isCompleted: Boolean) {
        val task = getTaskById(taskId) ?: return
        viewModelScope.launch {
            taskInteractors.update(
                task.id,
                task.goalId,
                task.title,
                task.description
                // TODO: update other fields
            )
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