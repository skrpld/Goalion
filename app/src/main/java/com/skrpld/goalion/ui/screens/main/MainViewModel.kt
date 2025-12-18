package com.skrpld.goalion.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrpld.goalion.data.GoalRepository
import com.skrpld.goalion.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: GoalRepository) : ViewModel() {

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask = _selectedTask.asStateFlow()

    private val _expandedGoalIds = MutableStateFlow<Set<Int>>(emptySet())
    val expandedGoalIds = _expandedGoalIds.asStateFlow()

    val uiState: StateFlow<MainUiState> = flow {
        val profile = repository.getProfile()
        if (profile != null) {
            repository.getGoalsWithTasks(profile.id).collect { goals ->
                emit(MainUiState.Success(goals))
            }
        } else {
            emit(MainUiState.Empty)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState.Loading
    )

    fun toggleGoalExpanded(goalId: Int) {
        _expandedGoalIds.value = _expandedGoalIds.value.let {
            if (it.contains(goalId)) it - goalId else it + goalId
        }
    }

    fun showTaskDetails(task: Task?) {
        _selectedTask.value = task
    }

    fun addGoal(title: String, profileId: Int) {
        viewModelScope.launch {
            val newGoal = Goal(title = title, profileId = profileId)
            repository.upsertGoal(newGoal)
        }
    }

    fun addTask(goalId: Int, title: String) {
        viewModelScope.launch {
            val newTask = Task(title = title, goalId = goalId, description = "")
            repository.upsertTask(newTask)
        }
    }

    fun updateGoalTitle(goal: Goal, newTitle: String) {
        viewModelScope.launch {
            repository.upsertGoal(goal.copy(title = newTitle))
        }
    }

    fun updateTaskDetails(task: Task, newTitle: String, newDescription: String) {
        viewModelScope.launch {
            repository.upsertTask(
                task.copy(title = newTitle, description = newDescription)
            )
        }
    }

    fun deleteGoal(goal: Goal) = viewModelScope.launch { repository.deleteGoal(goal) }
    fun deleteTask(task: Task) = viewModelScope.launch { repository.deleteTask(task) }

    fun reorderGoals(goalId: Int, newOrder: Int) {
        viewModelScope.launch { repository.updateGoalOrder(goalId, newOrder) }
    }

    fun reorderTasks(taskId: Int, newOrder: Int) {
        viewModelScope.launch { repository.updateTaskOrder(taskId, newOrder) }
    }
}

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val goals: List<GoalWithTasks>) : MainUiState()
    object Empty : MainUiState()
}