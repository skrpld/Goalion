package com.skrpld.goalion.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skrpld.goalion.data.database.AppDao
import com.skrpld.goalion.data.database.TaskPriority
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val profile: Profile = Profile(name = "Loading..."),
    val goals: List<GoalWithTasks> = emptyList(),
    val goalIdToEdit: Int? = null,
    val taskIdToEdit: Int? = null
)

class MainViewModel(
    private val dao: AppDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var currentProfileId: Int = 0

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            var profile = dao.getAnyProfile()
            if (profile == null) {
                val newId = dao.insertProfile(Profile(name = "User"))
                profile = Profile(newId.toInt(), "User")
            }

            currentProfileId = profile.id
            _uiState.update { it.copy(profile = profile) }

            dao.getGoalsWithTasksList(currentProfileId)
                .map { goals ->
                    goals.sortedWith(
                        compareBy<GoalWithTasks> {
                            if (it.goal.status == TaskStatus.TODO) 0 else 1
                        }.thenBy {
                            when (it.goal.priority) {
                                TaskPriority.HIGH -> 0
                                TaskPriority.NORMAL -> 1
                                TaskPriority.LOW -> 2
                            }
                        }.thenBy {
                            it.goal.orderIndex
                        }
                    )
                }
                .collect { sortedGoals ->
                    _uiState.update { it.copy(goals = sortedGoals) }
                }
        }
    }

    fun onAddGoalClick() {
        viewModelScope.launch {
            val newGoal = Goal(title = "", profileId = currentProfileId)
            val newId = dao.insertGoal(newGoal).toInt()
            _uiState.update { it.copy(goalIdToEdit = newId) }
        }
    }

    fun onGoalTitleChanged(goal: Goal, newTitle: String) {
        if (goal.title == newTitle) return
        viewModelScope.launch {
            dao.updateGoal(goal.copy(title = newTitle))
        }
    }

    fun onGoalEditStarted() {
        _uiState.update { it.copy(goalIdToEdit = null) }
    }

    fun onGoalStatusChange(goalId: Int, currentStatus: TaskStatus) {
        viewModelScope.launch {
            val newStatus = if (currentStatus == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
            dao.updateGoalStatus(goalId, newStatus)
        }
    }

    fun onGoalPriorityChange(goalId: Int, currentPriority: TaskPriority) {
        viewModelScope.launch {
            val nextPriority = when (currentPriority) {
                TaskPriority.LOW -> TaskPriority.NORMAL
                TaskPriority.NORMAL -> TaskPriority.HIGH
                TaskPriority.HIGH -> TaskPriority.LOW
            }
            dao.updateGoalPriority(goalId, nextPriority)
        }
    }

    fun onAddTaskClick(goalId: Int) {
        viewModelScope.launch {
            val newTask = Task(
                title = "",
                description = "",
                status = TaskStatus.TODO,
                priority = TaskPriority.NORMAL,
                goalId = goalId
            )
            val newId = dao.insertTask(newTask).toInt()
            _uiState.update { it.copy(taskIdToEdit = newId) }
        }
    }

    fun onTaskTitleChanged(task: Task, newTitle: String) {
        if (task.title == newTitle) return
        viewModelScope.launch {
            dao.updateTask(task.copy(title = newTitle))
        }
    }

    fun onTaskEditStarted() {
        _uiState.update { it.copy(taskIdToEdit = null) }
    }

    fun onTaskStatusChange(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
            dao.updateTaskStatus(task.id, newStatus)
        }
    }
    fun onTaskPriorityChange(task: Task) {
        viewModelScope.launch {
            val nextPriority = when (task.priority) {
                TaskPriority.LOW -> TaskPriority.NORMAL
                TaskPriority.NORMAL -> TaskPriority.HIGH
                TaskPriority.HIGH -> TaskPriority.LOW
            }

            dao.updateTaskPriority(task.id, nextPriority)
        }
    }

    fun onProfileClick() {
        // TODO: Открыть список профилей
    }
}

class MainViewModelFactory(private val dao: AppDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}