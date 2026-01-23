package com.skrpld.goalion.ui.screens.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrpld.goalion.data.local.AppDao
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.data.models.*
import com.skrpld.goalion.util.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class GoalListItem {
    data class GoalHeader(
        val goal: Goal,
        val tasks: List<Task> = emptyList(),
        val isExpanded: Boolean = false
    ) : GoalListItem()
    data class TaskItem(val task: Task) : GoalListItem()
}

class MainViewModel(private val dao: AppDao) : ViewModel() {

    private val _selectedActionItem = MutableStateFlow<ActionTarget?>(null)
    val selectedActionItem = _selectedActionItem.asStateFlow()

    private val _editingId = MutableStateFlow<String?>(null)
    val editingId = _editingId.asStateFlow()

    private val _selectedTaskForDetails = MutableStateFlow<Task?>(null)
    val selectedTaskForDetails = _selectedTaskForDetails.asStateFlow()

    private val _expandedGoalIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _pinnedGoalIds = MutableStateFlow<Set<Int>>(emptySet())
    val pinnedGoalIds = _pinnedGoalIds.asStateFlow()

    private var prioritySaveJob: Job? = null

    private val profileFlow = flow {
        var profile = dao.getAnyProfile()
        if (profile == null) {
            val newId = dao.upsertProfile(Profile(name = "Default"))
            profile = Profile(id = newId.toInt(), name = "Default")
        }
        emit(profile)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MainUiState> = profileFlow.flatMapLatest { profile ->
        combine(
            dao.getGoalsWithTasksList(profile.id),
            _expandedGoalIds
        ) { goalsWithTasks, expandedIds ->
            if (goalsWithTasks.isEmpty()) {
                MainUiState.Empty(profile.id)
            } else {
                val sortedGoals = goalsWithTasks.sortedWith(
                    compareBy<GoalWithTasks> { it.goal.status == TaskStatus.DONE }
                        .thenBy { it.goal.priority }
                        .thenByDescending { it.goal.updatedAt }
                )

                val items = sortedGoals.map { item ->
                    val sortedTasks = item.tasks.sortedWith(
                        compareBy<Task> { it.status == TaskStatus.DONE }
                            .thenBy { it.priority }
                            .thenByDescending { it.updatedAt }
                    )
                    GoalListItem.GoalHeader(
                        goal = item.goal,
                        tasks = sortedTasks,
                        isExpanded = expandedIds.contains(item.goal.id)
                    )
                }
                MainUiState.Success(items, profile.id, sortedGoals)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState.Loading)

    sealed class MainUiState {
        object Loading : MainUiState()
        data class Success(
            val items: List<GoalListItem.GoalHeader>,
            val profileId: Int,
            val rawData: List<GoalWithTasks>
        ) : MainUiState()
        data class Empty(val profileId: Int) : MainUiState()
    }

    private fun now() = System.currentTimeMillis()

    // --- Actions ---

    fun togglePinGoal(androidContext: Context) {
        val target = _selectedActionItem.value as? ActionTarget.GoalTarget ?: return
        val goalId = target.goal.id
        val helper = NotificationHelper(androidContext)

        if (_pinnedGoalIds.value.contains(goalId)) {
            helper.dismissNotification(goalId)
            _pinnedGoalIds.update { it - goalId }
        } else {
            val state = uiState.value
            if (state is MainUiState.Success) {
                state.rawData.find { it.goal.id == goalId }?.let {
                    helper.showGoalNotification(it)
                    _pinnedGoalIds.update { it + goalId }
                }
            }
        }
        _selectedActionItem.value = null
    }
    fun addGoal(profileId: Int) {
        viewModelScope.launch {
            val id = dao.upsertGoal(Goal(title = "", profileId = profileId, updatedAt = now())).toInt()
            startEditing(id, isGoal = true)
        }
    }

    fun addTask(goalId: Int) {
        viewModelScope.launch {
            val id = dao.upsertTask(
                Task(
                    title = "",
                    goalId = goalId,
                    description = "",
                    updatedAt = now()
                )
            ).toInt()
            _expandedGoalIds.update { it + goalId }
            startEditing(id, isGoal = false)
        }
    }

    fun startEditing(id: Int, isGoal: Boolean) {
        _editingId.value = if (isGoal) "goal_$id" else "task_$id"
        _selectedActionItem.value = null
    }

    fun stopEditing() {
        _editingId.value = null
    }

    fun selectActionItem(target: ActionTarget?) {
        _selectedActionItem.value = target
    }

    fun toggleGoalExpanded(goalId: Int) {
        _expandedGoalIds.update { if (it.contains(goalId)) it - goalId else it + goalId }
    }

    fun showTaskDetails(task: Task?) {
        _selectedTaskForDetails.value = task
    }

    fun updateGoalTitle(goal: Goal, title: String) = viewModelScope.launch {
        dao.upsertGoal(goal.copy(title = title, updatedAt = now()))
    }

    fun updateTaskTitle(task: Task, title: String) = viewModelScope.launch {
        dao.upsertTask(task.copy(title = title, updatedAt = now()))
    }

    fun updateTaskDescription(task: Task, desc: String) = viewModelScope.launch {
        dao.upsertTask(task.copy(description = desc, updatedAt = now()))
    }

    fun toggleStatus(target: ActionTarget) = viewModelScope.launch {
        when (target) {
            is ActionTarget.GoalTarget -> dao.upsertGoal(target.goal.copy(
                status = if (target.goal.status == TaskStatus.TODO) TaskStatus.DONE else TaskStatus.TODO,
                updatedAt = now()
            ))
            is ActionTarget.TaskTarget -> dao.upsertTask(target.task.copy(
                status = if (target.task.status == TaskStatus.TODO) TaskStatus.DONE else TaskStatus.TODO,
                updatedAt = now()
            ))
        }
    }

    fun cyclePriority() {
        val current = _selectedActionItem.value ?: return
        prioritySaveJob?.cancel()
        val updated = when (current) {
            is ActionTarget.GoalTarget -> current.copy(goal = current.goal.copy(priority = (current.goal.priority + 1) % 3, updatedAt = now()))
            is ActionTarget.TaskTarget -> current.copy(task = current.task.copy(priority = (current.task.priority + 1) % 3, updatedAt = now()))
        }
        _selectedActionItem.value = updated
        prioritySaveJob = viewModelScope.launch {
            when (val target = _selectedActionItem.value) {
                is ActionTarget.GoalTarget -> dao.upsertGoal(target.goal)
                is ActionTarget.TaskTarget -> dao.upsertTask(target.task)
                null -> {}
            }
        }
    }

    fun deleteCurrentTarget() = viewModelScope.launch {
        when (val current = _selectedActionItem.value) {
            is ActionTarget.GoalTarget -> {
                dao.deleteGoal(current.goal)
                _pinnedGoalIds.update { it - current.goal.id }
            }
            is ActionTarget.TaskTarget -> dao.deleteTask(current.task)
            null -> {}
        }
        _selectedActionItem.value = null
    }

    sealed class ActionTarget {
        data class GoalTarget(val goal: Goal) : ActionTarget()
        data class TaskTarget(val task: Task) : ActionTarget()
        val id: Int get() = when(this) { is GoalTarget -> goal.id; is TaskTarget -> task.id }
    }
}