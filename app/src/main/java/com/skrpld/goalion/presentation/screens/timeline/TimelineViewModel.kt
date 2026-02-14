package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrpld.goalion.domain.model.Goal
import com.skrpld.goalion.domain.model.Task
import com.skrpld.goalion.domain.usecases.GoalInteractors
import com.skrpld.goalion.domain.usecases.TaskInteractors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

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
        viewModelScope.launch {
            delay(100)
            centerOnToday()
        }
    }

    private fun observeData() {
        _uiState.update { it.copy(isLoading = true) }

        combine(
            goalInteractors.getWithTasks(profileId),
            _uiState.map { it.showCompleted }.distinctUntilChanged()
        ) { dbData, showCompleted ->
            val expandedIds = _uiState.value.goals
                .filter { it.isExpanded }
                .map { it.data.goal.id }
                .toSet()

            val filteredData = dbData.filter { item ->
                item.goal.status == showCompleted
            }

            filteredData.map { goalWithTasks ->
                TimelineGoalItem(
                    data = goalWithTasks,
                    isExpanded = goalWithTasks.goal.id in expandedIds
                )
            }
        }.onEach { items ->
            _uiState.update { it.copy(goals = items, isLoading = false) }
        }.launchIn(viewModelScope)
    }

    private fun getGoalById(goalId: String) =
        _uiState.value.goals.find { it.data.goal.id == goalId }?.data?.goal

    private fun getTaskById(taskId: String) = _uiState.value.goals
        .flatMap { it.data.tasks }
        .find { it.id == taskId }

    // --- UI Control Actions ---

    fun toggleShowCompleted() {
        _uiState.update { it.copy(showCompleted = !it.showCompleted) }
    }

    fun cycleZoomLevel() {
        _uiState.update { state ->
            val newZoom = if (state.zoomLevel == ZoomLevel.NORMAL) ZoomLevel.DETAILED else ZoomLevel.NORMAL
            state.copy(zoomLevel = newZoom)
        }
        centerOnToday()
    }

    fun centerOnToday() {
        _uiState.update { it.copy(scrollRequest = System.currentTimeMillis()) }
    }

    fun consumeScrollRequest() {
        _uiState.update { it.copy(scrollRequest = null) }
    }

    fun setScroll(x: Float, y: Float) {
        scrollOffsetX = x
        scrollOffsetY = y.coerceAtLeast(0f)
    }

    fun onScroll(delta: Float, orientation: Orientation) {
        if (orientation == Orientation.Vertical) {
            scrollOffsetY = (scrollOffsetY - delta).coerceAtLeast(0f)
        } else {
            scrollOffsetX -= delta
        }
    }

    // --- Status & Priority Actions ---

    fun toggleGoalStatus(goal: Goal) {
        manageGoal(id = goal.id, status = !goal.status)
    }

    fun toggleTaskStatus(task: Task) {
        manageTask(id = task.id, status = !task.status)
    }

    // --- Dialogs / BottomSheet ---

    fun openCreateGoalDialog() {
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                dialogState = DialogState(
                    isOpen = true,
                    mode = EditMode.CREATE_GOAL,
                    initialStartDate = now,
                    initialTargetDate = now + MILLIS_IN_DAY * 7,
                    initialPriority = 1
                )
            )
        }
    }

    fun openEditGoalDialog(goal: Goal) {
        _uiState.update {
            it.copy(
                dialogState = DialogState(
                    isOpen = true,
                    mode = EditMode.EDIT_GOAL,
                    entityId = goal.id,
                    initialTitle = goal.title,
                    initialDescription = goal.description,
                    initialStartDate = goal.startDate,
                    initialTargetDate = goal.targetDate,
                    initialPriority = goal.priority
                )
            )
        }
    }

    fun openCreateTaskDialog(goalId: String) {
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                dialogState = DialogState(
                    isOpen = true,
                    mode = EditMode.CREATE_TASK,
                    parentId = goalId,
                    initialStartDate = now,
                    initialTargetDate = now + MILLIS_IN_DAY * 3,
                    initialPriority = 1
                )
            )
        }
    }

    fun openEditTaskDialog(task: Task) {
        _uiState.update {
            it.copy(
                dialogState = DialogState(
                    isOpen = true,
                    mode = EditMode.EDIT_TASK,
                    entityId = task.id,
                    parentId = task.goalId,
                    initialTitle = task.title,
                    initialDescription = task.description,
                    initialStartDate = task.startDate,
                    initialTargetDate = task.targetDate,
                    initialPriority = task.priority
                )
            )
        }
    }

    fun openViewTask(task: Task) {
        _uiState.update {
            it.copy(
                dialogState = DialogState(
                    isOpen = true,
                    mode = EditMode.VIEW_TASK,
                    entityId = task.id,
                    parentId = task.goalId,
                    initialTitle = task.title,
                    initialDescription = task.description,
                    initialStartDate = task.startDate,
                    initialTargetDate = task.targetDate,
                    initialPriority = task.priority
                )
            )
        }
    }

    fun closeDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isOpen = false)) }
    }

    fun onSaveDialog(title: String, description: String, start: Long, target: Long, priority: Int) {
        val state = _uiState.value.dialogState
        when (state.mode) {
            EditMode.CREATE_GOAL -> manageGoal(
                title = title, description = description,
                startDate = start, targetDate = target, priority = priority
            )
            EditMode.EDIT_GOAL -> manageGoal(
                id = state.entityId, title = title, description = description,
                startDate = start, targetDate = target, priority = priority
            )
            EditMode.CREATE_TASK -> manageTask(
                goalId = state.parentId, title = title, description = description,
                startDate = start, targetDate = target, priority = priority
            )
            EditMode.EDIT_TASK -> manageTask(
                id = state.entityId, goalId = state.parentId, title = title, description = description,
                startDate = start, targetDate = target, priority = priority
            )
            EditMode.VIEW_TASK -> {}
        }
        closeDialog()
    }

    fun onDeleteFromDialog() {
        val state = _uiState.value.dialogState
        when (state.mode) {
            EditMode.EDIT_GOAL -> state.entityId?.let { deleteGoal(it) }
            EditMode.EDIT_TASK -> state.entityId?.let { deleteTask(it) }
            else -> Unit
        }
        closeDialog()
    }

    // --- Goals Management ---

    fun manageGoal(
        id: String? = null,
        title: String? = null,
        description: String? = null,
        startDate: Long? = null,
        targetDate: Long? = null,
        status: Boolean? = null,
        priority: Int? = null,
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
                        id = UUID.randomUUID().toString(),
                        profileId = profileId,
                        title = title ?: "New Goal",
                        description = description ?: "",
                        status = false,
                        priority = priority ?: 1,
                        order = 0,
                        startDate = startDate ?: System.currentTimeMillis(),
                        targetDate = targetDate ?: (System.currentTimeMillis() + MILLIS_IN_DAY * 7)
                    )
                } else {
                    val goal = getGoalById(id) ?: return@launch
                    goalInteractors.update(
                        id = goal.id,
                        profileId = goal.profileId,
                        title = title ?: goal.title,
                        description = description ?: goal.description,
                        status = status ?: goal.status,
                        priority = priority ?: goal.priority,
                        order = goal.order,
                        startDate = startDate ?: goal.startDate,
                        targetDate = targetDate ?: goal.targetDate
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

    // --- Tasks Management ---

    fun manageTask(
        id: String? = null,
        goalId: String? = null,
        title: String? = null,
        description: String? = null,
        startDate: Long? = null,
        targetDate: Long? = null,
        status: Boolean? = null,
        priority: Int? = null,
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
                        id = UUID.randomUUID().toString(),
                        goalId = goalId,
                        title = title ?: "New Task",
                        description = description ?: "",
                        status = false,
                        priority = priority ?: 1,
                        order = 0,
                        startDate = startDate ?: System.currentTimeMillis(),
                        targetDate = targetDate ?: (System.currentTimeMillis() + MILLIS_IN_DAY * 3)
                    )
                    val index = _uiState.value.goals.indexOfFirst { it.data.goal.id == goalId }
                    if (index != -1 && !_uiState.value.goals[index].isExpanded) {
                        toggleGoalExpansion(index)
                    }
                } else if (id != null) {
                    val task = getTaskById(id) ?: return@launch
                    taskInteractors.update(
                        id = task.id,
                        goalId = task.goalId,
                        title = title ?: task.title,
                        description = description ?: task.description,
                        status = status ?: task.status,
                        priority = priority ?: task.priority,
                        order = task.order,
                        startDate = startDate ?: task.startDate,
                        targetDate = targetDate ?: task.targetDate
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

    // --- UI Helper Logic ---

    fun toggleGoalExpansion(index: Int) {
        _uiState.update { state ->
            val newList = state.goals.toMutableList()
            if (index in newList.indices) {
                newList[index] = newList[index].copy(isExpanded = !newList[index].isExpanded)
            }
            state.copy(goals = newList)
        }
    }
}