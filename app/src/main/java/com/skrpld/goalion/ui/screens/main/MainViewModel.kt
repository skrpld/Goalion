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
        viewModelScope.launch {
            initializeData()
        }
    }

    private suspend fun initializeData() {
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
                            when (it.goal.status) {
                                TaskStatus.TODO -> 0
                                TaskStatus.DONE -> 1
                            }
                        }.thenBy {
                            when (it.goal.priority) {
                                TaskPriority.HIGH -> 0
                                TaskPriority.NORMAL -> 1
                                TaskPriority.LOW -> 2
                            }
                        }
                    )
                }
                .collect { sortedGoals ->
                    _uiState.update { it.copy(goals = sortedGoals) }
                }
        }
        dao.getGoalsWithTasksList(currentProfileId)
            .map { goals ->
                goals.sortedWith(
                    compareBy<GoalWithTasks> {
                        when (it.goal.status) {
                            TaskStatus.TODO -> 0
                            TaskStatus.DONE -> 1
                        }
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

    fun onGoalReorder(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        val currentList = _uiState.value.goals.toMutableList()
        val itemToMove = currentList[fromIndex]

        val targetIndex = clampIndexToGroup(currentList, itemToMove, toIndex)

        if (fromIndex == targetIndex) return

        currentList.removeAt(fromIndex)
        currentList.add(targetIndex, itemToMove)

        _uiState.update { it.copy(goals = currentList) }

        viewModelScope.launch {
            val groupItems = currentList.filter {
                it.goal.status == itemToMove.goal.status &&
                        it.goal.priority == itemToMove.goal.priority
            }

            groupItems.forEachIndexed { index, goalWithTasks ->
                if (goalWithTasks.goal.orderIndex != index) {
                    dao.updateGoalOrder(goalWithTasks.goal.id, index)
                }
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

    fun onTaskReorder(goalId: Int, fromIndex: Int, toIndex: Int) {
        val goalWithTasks = _uiState.value.goals.find { it.goal.id == goalId } ?: return
        val currentTasks = goalWithTasks.tasks.sortedWith(
            compareBy<Task> { if (it.status == TaskStatus.TODO) 0 else 1 }
                .thenBy {
                    when (it.priority) {
                        TaskPriority.HIGH -> 0
                        TaskPriority.NORMAL -> 1
                        TaskPriority.LOW -> 2
                    }
                }
                .thenBy { it.orderIndex }
        ).toMutableList()

        if (fromIndex !in currentTasks.indices || toIndex !in currentTasks.indices) return

        val taskToMove = currentTasks[fromIndex]

        val targetIndex = clampTaskIndexToGroup(currentTasks, taskToMove, toIndex)
        if (fromIndex == targetIndex) return

        currentTasks.removeAt(fromIndex)
        currentTasks.add(targetIndex, taskToMove)

        viewModelScope.launch {
            val groupItems = currentTasks.filter {
                it.status == taskToMove.status && it.priority == taskToMove.priority
            }
            groupItems.forEachIndexed { index, task ->
                if (task.orderIndex != index) {
                    dao.updateTaskOrder(task.id, index)
                }
            }
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

    private fun clampIndexToGroup(list: List<GoalWithTasks>, item: GoalWithTasks, targetIndex: Int): Int {
        val targetItem = list.getOrNull(targetIndex) ?: return targetIndex

        if (targetItem.goal.status == item.goal.status &&
            targetItem.goal.priority == item.goal.priority) {
            return targetIndex
        }

        return if (targetIndex > list.indexOf(item)) {
            list.indexOfLast {
                it.goal.status == item.goal.status && it.goal.priority == item.goal.priority
            }
        } else {
            list.indexOfFirst {
                it.goal.status == item.goal.status && it.goal.priority == item.goal.priority
            }
        }.let { if (it == -1) list.indexOf(item) else it }
    }

    private fun clampTaskIndexToGroup(list: List<Task>, item: Task, targetIndex: Int): Int {
        val targetItem = list.getOrNull(targetIndex) ?: return targetIndex
        if (targetItem.status == item.status && targetItem.priority == item.priority) return targetIndex

        return if (targetIndex > list.indexOf(item)) {
            list.indexOfLast { it.status == item.status && it.priority == item.priority }
        } else {
            list.indexOfFirst { it.status == item.status && it.priority == item.priority }
        }.let { if (it == -1) list.indexOf(item) else it }
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