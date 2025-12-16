package com.skrpld.goalion.ui.screens.main

import androidx.lifecycle.ViewModel
import com.skrpld.goalion.data.database.TaskPriority
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Состояние экрана
data class MainUiState(
    val profile: Profile = Profile(name = "User"), // Заглушка
    val goals: List<GoalWithTasks> = emptyList()
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // TODO: Подписаться на Flow из Room
        loadMockData() // Тестовые данные
    }

    private fun loadMockData() {
        val mockTasks = listOf(
            Task(1, "Read Chapter 1", "Basics", TaskStatus.TODO, TaskPriority.HIGH, 1),
            Task(2, "Do exercises", "Page 20", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, 1)
        )
        val mockGoal = Goal(1, "Learn Kotlin", 1)

        val goalWithTasks = GoalWithTasks(mockGoal, mockTasks)

        _uiState.value = MainUiState(
            profile = Profile(1, "Alex"),
            goals = listOf(goalWithTasks, goalWithTasks.copy(goal = Goal(2, "Sport", 1), tasks = emptyList()))
        )
    }

    fun onAddGoalClick() {

    }

    fun onProfileClick() {

    }

    fun onAddTaskClick(goalId: Int) {

    }
}