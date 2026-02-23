package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.gestures.Orientation
import androidx.lifecycle.SavedStateHandle
import com.skrpld.goalion.domain.model.Goal
import com.skrpld.goalion.domain.model.GoalWithTasks
import com.skrpld.goalion.domain.model.Task
import com.skrpld.goalion.domain.usecases.DeleteGoalUseCase
import com.skrpld.goalion.domain.usecases.DeleteTaskUseCase
import com.skrpld.goalion.domain.usecases.GetGoalsWithTasksUseCase
import com.skrpld.goalion.domain.usecases.GoalInteractors
import com.skrpld.goalion.domain.usecases.TaskInteractors
import com.skrpld.goalion.domain.usecases.UpdateGoalUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskStatusUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskUseCase
import com.skrpld.goalion.presentation.screens.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var getGoalsUseCase: GetGoalsWithTasksUseCase
    private lateinit var updateGoalUseCase: UpdateGoalUseCase
    private lateinit var deleteGoalUseCase: DeleteGoalUseCase

    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var updateTaskStatusUseCase: UpdateTaskStatusUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase

    private lateinit var goalInteractors: GoalInteractors
    private lateinit var taskInteractors: TaskInteractors

    private lateinit var viewModel: TimelineViewModel

    private val profileId = "profile_123"

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.w(any(), any<String>(), any()) } returns 0

        savedStateHandle = SavedStateHandle(mapOf("profileId" to profileId))

        getGoalsUseCase = mockk(relaxed = true)
        updateGoalUseCase = mockk(relaxed = true)
        deleteGoalUseCase = mockk(relaxed = true)

        goalInteractors = GoalInteractors(
            getWithTasks = getGoalsUseCase,
            create = mockk(relaxed = true),
            update = updateGoalUseCase,
            delete = deleteGoalUseCase,
            sync = mockk(relaxed = true),
            updateStatus = mockk(relaxed = true),
            updatePriority = mockk(relaxed = true),
            updateOrder = mockk(relaxed = true),
            updateTitle = mockk(relaxed = true),
            updateDescription = mockk(relaxed = true),
            updateStartDate = mockk(relaxed = true),
            updateTargetDate = mockk(relaxed = true)
        )

        updateTaskUseCase = mockk(relaxed = true)
        updateTaskStatusUseCase = mockk(relaxed = true)
        deleteTaskUseCase = mockk(relaxed = true)

        taskInteractors = TaskInteractors(
            create = mockk(relaxed = true),
            update = updateTaskUseCase,
            delete = deleteTaskUseCase,
            sync = mockk(relaxed = true),
            updateStatus = updateTaskStatusUseCase,
            updatePriority = mockk(relaxed = true),
            updateOrder = mockk(relaxed = true),
            updateTitle = mockk(relaxed = true),
            updateDescription = mockk(relaxed = true),
            updateStartDate = mockk(relaxed = true),
            updateTargetDate = mockk(relaxed = true)
        )
    }

    @After
    fun teardown() {
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `test_01_init_loadById`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        verify { getGoalsUseCase.invoke(profileId) }
    }

    @Test
    fun `test_02_flow_updateGoals`() = runTest {
        val goalWithTasks = GoalWithTasks(Goal(id = "g1", profileId = profileId, title = "T"), emptyList())
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(listOf(goalWithTasks))

        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.goals.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `test_03_filter_showCompleted`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        val initialStatus = viewModel.uiState.value.showCompleted
        viewModel.toggleShowCompleted()
        advanceUntilIdle()

        assertNotEquals(initialStatus, viewModel.uiState.value.showCompleted)
    }

    @Test
    fun `test_04_zoom_cycleLevel`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        assertEquals(ZoomLevel.NORMAL, viewModel.uiState.value.zoomLevel)
        viewModel.cycleZoomLevel()
        advanceUntilIdle()

        assertEquals(ZoomLevel.DETAILED, viewModel.uiState.value.zoomLevel)
        assertNotNull(viewModel.uiState.value.scrollRequest)
    }

    @Test
    fun `test_05_nav_centerOnToday`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        viewModel.consumeScrollRequest()
        viewModel.centerOnToday()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.scrollRequest)
    }

    @Test
    fun `test_06_nav_consumeScrollRequest`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        viewModel.centerOnToday()
        viewModel.consumeScrollRequest()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.scrollRequest)
    }

    @Test
    fun `test_07_scroll_bounds`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        viewModel.onScroll(100f, Orientation.Vertical)
        assertEquals(0f, viewModel.scrollOffsetY)
    }

    @Test
    fun `test_08_dialog_createGoal`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        viewModel.openCreateGoalDialog()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.dialogState.isOpen)
        assertEquals(EditMode.CREATE_GOAL, viewModel.uiState.value.dialogState.mode)
    }

    @Test
    fun `test_09_dialog_editTask`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        val task = Task(id = "t1", goalId = "g1", title = "Task")
        viewModel.openEditTaskDialog(task)
        advanceUntilIdle()

        val state = viewModel.uiState.value.dialogState
        assertTrue(state.isOpen)
        assertEquals(EditMode.EDIT_TASK, state.mode)
        assertEquals("t1", state.entityId)
    }

    @Test
    fun `test_10_crud_createGoalSave`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        viewModel.openCreateGoalDialog()
        viewModel.onSaveDialog("New Goal", "Desc", 0L, 0L, 1)
        advanceUntilIdle()

        coVerify { updateGoalUseCase.invoke(any(), profileId, "New Goal", "Desc", false, 1, 0, 0L, 0L) }
        assertFalse(viewModel.uiState.value.dialogState.isOpen)
    }

    @Test
    fun `test_11_crud_editTaskSave`() = runTest {
        val task = Task(id = "t1", goalId = "g1", title = "T", description = "D")
        val goalWithTasks = GoalWithTasks(Goal(id="g1", profileId="p", title=""), listOf(task))
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(listOf(goalWithTasks))
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        viewModel.openEditTaskDialog(task)
        viewModel.onSaveDialog("Updated", "Desc", 0L, 0L, 1)
        advanceUntilIdle()

        coVerify { updateTaskUseCase.invoke("t1", "g1", "Updated", "Desc", false, 1, 0, 0L, 0L) }
        assertFalse(viewModel.uiState.value.dialogState.isOpen)
    }

    @Test
    fun `test_12_crud_deleteFromDialog`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        val goal = Goal(id = "g1", profileId = profileId, title = "Goal")
        viewModel.openEditGoalDialog(goal)
        viewModel.onDeleteFromDialog()
        advanceUntilIdle()

        coVerify { deleteGoalUseCase.invoke("g1") }
        assertFalse(viewModel.uiState.value.dialogState.isOpen)
    }

    @Test
    fun `test_13_status_toggleGoal`() = runTest {
        val goal = Goal(id = "g1", profileId = profileId, title = "Goal", status = false)
        val goalWithTasks = GoalWithTasks(goal, emptyList())
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(listOf(goalWithTasks))
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        viewModel.toggleGoalStatus(goal)
        advanceUntilIdle()

        coVerify { updateGoalUseCase.invoke(id="g1", status=true, profileId=any(), title=any(), description=any(), priority=any(), order=any(), startDate=any(), targetDate=any()) }
    }

    @Test
    fun `test_14_status_toggleTask`() = runTest {
        val task = Task(id = "t1", goalId = "g1", title = "Task", status = true)
        val goalWithTasks = GoalWithTasks(Goal(id="g1", profileId="p", title=""), listOf(task))
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(listOf(goalWithTasks))
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        viewModel.toggleTaskStatus(task)
        advanceUntilIdle()

        coVerify { updateTaskUseCase.invoke(id="t1", status=false, goalId=any(), title=any(), description=any(), priority=any(), order=any(), startDate=any(), targetDate=any()) }
    }

    @Test
    fun `test_15_ui_expandCollapse`() = runTest {
        val goalWithTasks = GoalWithTasks(Goal(id = "g1", profileId = profileId, title = ""), emptyList())
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(listOf(goalWithTasks))
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        val initialExpand = viewModel.uiState.value.goals[0].isExpanded
        viewModel.toggleGoalExpansion(0)
        advanceUntilIdle()

        assertNotEquals(initialExpand, viewModel.uiState.value.goals[0].isExpanded)
    }

    @Test
    fun `test_16_errorHandling`() = runTest {
        every { getGoalsUseCase.invoke(profileId) } returns flowOf(emptyList())
        viewModel = TimelineViewModel(savedStateHandle, goalInteractors, taskInteractors)
        advanceUntilIdle()

        coEvery { deleteGoalUseCase.invoke(any()) } throws Exception("Delete failed")

        viewModel.manageGoal(id = "g1", isDelete = true)
        advanceUntilIdle()

        assertEquals("Delete failed", viewModel.uiState.value.errorMessage)
    }
}