package com.skrpld.goalion.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrpld.goalion.data.database.AppDao
import com.skrpld.goalion.data.models.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val dao: AppDao) : ViewModel() {

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask = _selectedTask.asStateFlow()

    private val _expandedGoalIds = MutableStateFlow<Set<Int>>(emptySet())

    private val profileFlow = flow { emit(dao.getAnyProfile()) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MainUiState> = profileFlow.flatMapLatest { profile ->
        if (profile == null) {
            flowOf(MainUiState.Empty)
        } else {
            combine(
                dao.getGoalsWithTasksList(profile.id),
                _expandedGoalIds
            ) { goalsWithTasks, expandedIds ->

                val flatList = mutableListOf<GoalListItem>()

                goalsWithTasks.forEach { item ->
                    val isExpanded = expandedIds.contains(item.goal.id)

                    flatList.add(GoalListItem.GoalHeader(item.goal, isExpanded))

                    if (isExpanded) {
                        val sortedTasks = item.tasks.sortedBy { it.orderIndex }
                        flatList.addAll(sortedTasks.map { GoalListItem.TaskItem(it) })
                    }
                }

                if (flatList.isEmpty()) MainUiState.Empty
                else MainUiState.Success(flatList, profile.id)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState.Loading
    )

    sealed class MainUiState {
        object Loading : MainUiState()
        data class Success(val items: List<GoalListItem>, val profileId: Int) : MainUiState()
        object Empty : MainUiState()
    }

    // --- fun ---

    fun toggleGoalExpanded(goalId: Int) {
        _expandedGoalIds.update { current ->
            if (current.contains(goalId)) current - goalId else current + goalId
        }
    }


    fun showTaskDetails(task: Task?) {
        _selectedTask.value = task
    }

    fun addGoal(title: String, profileId: Int) {
        viewModelScope.launch {
            val newGoal = Goal(title = title, profileId = profileId, orderIndex = 0)
            dao.upsertGoal(newGoal)
        }
    }

    fun addTask(goalId: Int, title: String) {
        viewModelScope.launch {
            val newTask = Task(title = title, goalId = goalId, description = "", orderIndex = 0)
            dao.upsertTask(newTask)
        }
    }

    fun updateGoalTitle(goal: Goal, newTitle: String) {
        viewModelScope.launch {
            dao.upsertGoal(goal.copy(title = newTitle))
        }
    }

    fun updateTaskDetails(task: Task, newTitle: String, newDescription: String) {
        viewModelScope.launch {
            dao.upsertTask(
                task.copy(title = newTitle, description = newDescription)
            )
        }
    }

    fun deleteGoal(goal: Goal) = viewModelScope.launch { dao.deleteGoal(goal) }
    fun deleteTask(task: Task) = viewModelScope.launch { dao.deleteTask(task) }

    fun reorderGoals(goalId: Int, newOrder: Int) {
        viewModelScope.launch { dao.updateGoalOrder(goalId, newOrder) }
    }

    fun reorderTasks(taskId: Int, newOrder: Int) {
        viewModelScope.launch { dao.updateTaskOrder(taskId, newOrder) }
    }
}

sealed class GoalListItem {
    data class GoalHeader(val goal: Goal, val isExpanded: Boolean) : GoalListItem()
    data class TaskItem(val task: Task) : GoalListItem()
}