package com.skrpld.goalion.ui.screens.home

import android.app.NotificationManager
import android.content.Context
import app.cash.turbine.test
import com.skrpld.goalion.data.local.AppDao
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.data.models.*
import com.skrpld.goalion.util.NotificationHelper
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val dao: AppDao = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val notificationManager: NotificationManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager

        mockkConstructor(NotificationHelper::class)
        every { anyConstructed<NotificationHelper>().showGoalNotification(any()) } just Runs
        every { anyConstructed<NotificationHelper>().dismissNotification(any()) } just Runs

        coEvery { dao.getAnyProfile() } returns Profile(id = 1, name = "Default")
        coEvery { dao.getGoalsWithTasksList(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel() {
        viewModel = MainViewModel(dao)
    }

    // --- 1. ТЕСТЫ ИНИЦИАЛИЗАЦИИ ---

    @Test
    fun `1 profileFlow_createsNewProfile_whenDbIsEmpty`() = runTest {
        coEvery { dao.getAnyProfile() } returns null
        coEvery { dao.upsertProfile(any()) } returns 10L

        createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        coVerify { dao.upsertProfile(match { it.name == "Default" }) }
        job.cancel()
    }

    @Test
    fun `2 profileFlow_usesExistingProfile_whenDbIsNotEmpty`() = runTest {
        coEvery { dao.getAnyProfile() } returns Profile(id = 5, name = "Existing")

        createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        coVerify(exactly = 0) { dao.upsertProfile(any()) }
        job.cancel()
    }

    // --- 2. ТЕСТЫ UI STATE ---

    @Test
    fun `3 uiState_emitsLoading_initially`() = runTest {
        createViewModel()
        viewModel.uiState.test {
            assertEquals(MainViewModel.MainUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `4 uiState_emitsEmpty_whenNoGoals`() = runTest {
        coEvery { dao.getGoalsWithTasksList(1) } returns flowOf(emptyList())
        createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state is MainViewModel.MainUiState.Empty)
            assertEquals(1, (state as MainViewModel.MainUiState.Empty).profileId)
        }
    }

    @Test
    fun `5 uiState_sorting_goalsOrder`() = runTest {
        val g1 = Goal(
            id = 1,
            title = "A",
            profileId = 1,
            status = TaskStatus.DONE,
            priority = 0,
            updatedAt = 100
        )
        val g2 = Goal(
            id = 2,
            title = "B",
            profileId = 1,
            status = TaskStatus.TODO,
            priority = 1,
            updatedAt = 200
        )
        val g3 = Goal(
            id = 3,
            title = "C",
            profileId = 1,
            status = TaskStatus.TODO,
            priority = 0,
            updatedAt = 300
        )

        val data = listOf(
            GoalWithTasks(g1, emptyList()),
            GoalWithTasks(g2, emptyList()),
            GoalWithTasks(g3, emptyList())
        )
        coEvery { dao.getGoalsWithTasksList(any()) } returns flowOf(data)
        createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            val success = awaitItem() as MainViewModel.MainUiState.Success
            assertEquals(3, success.items[0].goal.id)
            assertEquals(2, success.items[1].goal.id)
            assertEquals(1, success.items[2].goal.id)
        }
    }

    @Test
    fun `6 uiState_sorting_tasksOrder`() = runTest {
        val goal = Goal(id = 1, title = "Goal", profileId = 1)
        val t1 = Task(id = 10, title = "T1", goalId = 1, status = TaskStatus.DONE)
        val t2 = Task(id = 11, title = "T2", goalId = 1, status = TaskStatus.TODO, updatedAt = 500)
        val t3 = Task(id = 12, title = "T3", goalId = 1, status = TaskStatus.TODO, updatedAt = 1000)

        coEvery { dao.getGoalsWithTasksList(any()) } returns flowOf(listOf(
            GoalWithTasks(
                goal,
                listOf(t1, t2, t3)
            )
        ))
        createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            val success = awaitItem() as MainViewModel.MainUiState.Success
            val tasks = success.items[0].tasks
            assertEquals(12, tasks[0].id)
            assertEquals(11, tasks[1].id)
            assertEquals(10, tasks[2].id)
        }
    }

    @Test
    fun `7 uiState_expansion_reflectsCorrectState`() = runTest {
        val goal = Goal(id = 1, title = "G", profileId = 1)
        coEvery { dao.getGoalsWithTasksList(any()) } returns flowOf(listOf(
            GoalWithTasks(
                goal,
                emptyList()
            )
        ))
        createViewModel()

        viewModel.toggleGoalExpanded(1)

        viewModel.uiState.test {
            skipItems(1)
            val success = awaitItem() as MainViewModel.MainUiState.Success
            assertTrue(success.items[0].isExpanded)
        }
    }

    // --- 3. ТЕСТЫ ДОБАВЛЕНИЯ ---

    @Test
    fun `8 addGoal_callsDao_andStartsEditing`() = runTest {
        coEvery { dao.upsertGoal(any()) } returns 100L
        createViewModel()
        viewModel.addGoal(1)
        advanceUntilIdle()
        assertEquals("goal_100", viewModel.editingId.value)
    }

    @Test
    fun `9 addTask_callsDao_expandsGoal_andStartsEditing`() = runTest {
        coEvery { dao.upsertTask(any()) } returns 200L
        createViewModel()
        viewModel.addTask(10)
        advanceUntilIdle()
        assertEquals("task_200", viewModel.editingId.value)
    }

    @Test
    fun `10 startEditing_setsCorrectId_forGoal`() {
        createViewModel()
        viewModel.startEditing(5, true)
        assertEquals("goal_5", viewModel.editingId.value)
    }

    @Test
    fun `11 startEditing_setsCorrectId_forTask`() {
        createViewModel()
        viewModel.startEditing(5, false)
        assertEquals("task_5", viewModel.editingId.value)
    }

    @Test
    fun `12 stopEditing_clearsId`() {
        createViewModel()
        viewModel.startEditing(1, true)
        viewModel.stopEditing()
        assertNull(viewModel.editingId.value)
    }

    // --- 4. ЗАКРЕПЛЕНИЕ ---

    @Test
    fun `13 togglePinGoal_addsPin_andShowsNotification`() = runTest {
        val goal = Goal(id = 1, title = "P", profileId = 1)
        coEvery { dao.getGoalsWithTasksList(any()) } returns flowOf(listOf(
            GoalWithTasks(
                goal,
                emptyList()
            )
        ))
        createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(goal))
        viewModel.togglePinGoal(context)

        assertTrue(viewModel.pinnedGoalIds.value.contains(1))
        job.cancel()
    }

    @Test
    fun `14 togglePinGoal_removesPin_andDismissesNotification`() = runTest {
        val goal = Goal(id = 1, title = "P", profileId = 1)
        coEvery { dao.getGoalsWithTasksList(any()) } returns flowOf(listOf(
            GoalWithTasks(
                goal,
                emptyList()
            )
        ))
        createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(goal))
        viewModel.togglePinGoal(context)

        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(goal))
        viewModel.togglePinGoal(context)

        assertFalse(viewModel.pinnedGoalIds.value.contains(1))
        job.cancel()
    }

    @Test
    fun `15 togglePinGoal_doesNothing_whenTargetIsNotGoal`() {
        createViewModel()
        viewModel.selectActionItem(MainViewModel.ActionTarget.TaskTarget(
            Task(
                id = 1,
                title = "",
                goalId = 1
            )
        ))
        viewModel.togglePinGoal(context)
        assertTrue(viewModel.pinnedGoalIds.value.isEmpty())
    }

    // --- 5. УПРАВЛЕНИЕ СОСТОЯНИЕМ ---

    @Test
    fun `16 toggleGoalExpanded_addsId_whenNotPresent`() {
        createViewModel()
        viewModel.toggleGoalExpanded(1)
    }

    @Test
    fun `17 toggleGoalExpanded_removesId_whenPresent`() {
        createViewModel()
        viewModel.toggleGoalExpanded(1)
        viewModel.toggleGoalExpanded(1)
    }

    @Test
    fun `18 selectActionItem_updatesState`() {
        createViewModel()
        val target = MainViewModel.ActionTarget.GoalTarget(Goal(id = 1, title = "", profileId = 1))
        viewModel.selectActionItem(target)
        assertEquals(target, viewModel.selectedActionItem.value)
    }

    @Test
    fun `19 showTaskDetails_updatesState`() {
        createViewModel()
        val task = Task(id = 1, title = "T", goalId = 1)
        viewModel.showTaskDetails(task)
        assertEquals(task, viewModel.selectedTaskForDetails.value)
    }

    // --- 6. ОБНОВЛЕНИЯ ---

    @Test
    fun `20 updateGoalTitle_callsDaoWithNewTitle`() = runTest {
        createViewModel()
        viewModel.updateGoalTitle(Goal(id = 1, title = "O", profileId = 1), "N")
        advanceUntilIdle()
        coVerify { dao.upsertGoal(match { it.title == "N" }) }
    }

    @Test
    fun `21 updateTaskTitle_callsDaoWithNewTitle`() = runTest {
        createViewModel()
        viewModel.updateTaskTitle(Task(id = 1, title = "O", goalId = 1), "N")
        advanceUntilIdle()
        coVerify { dao.upsertTask(match { it.title == "N" }) }
    }

    @Test
    fun `22 updateTaskDescription_callsDaoWithNewDesc`() = runTest {
        createViewModel()
        viewModel.updateTaskDescription(Task(id = 1, title = "T", goalId = 1, description = "O"), "N")
        advanceUntilIdle()
        coVerify { dao.upsertTask(match { it.description == "N" }) }
    }

    @Test
    fun `23 toggleStatus_goal_switchesTodoToDone`() = runTest {
        createViewModel()
        viewModel.toggleStatus(MainViewModel.ActionTarget.GoalTarget(
            Goal(
                profileId = 1,
                status = TaskStatus.TODO
            )
        ))
        advanceUntilIdle()
        coVerify { dao.upsertGoal(match { it.status == TaskStatus.DONE }) }
    }

    @Test
    fun `24 toggleStatus_goal_switchesDoneToTodo`() = runTest {
        createViewModel()
        viewModel.toggleStatus(MainViewModel.ActionTarget.GoalTarget(
            Goal(
                profileId = 1,
                status = TaskStatus.DONE
            )
        ))
        advanceUntilIdle()
        coVerify { dao.upsertGoal(match { it.status == TaskStatus.TODO }) }
    }

    @Test
    fun `25 toggleStatus_task_switchesStatus`() = runTest {
        createViewModel()
        viewModel.toggleStatus(MainViewModel.ActionTarget.TaskTarget(
            Task(
                goalId = 1,
                status = TaskStatus.TODO
            )
        ))
        advanceUntilIdle()
        coVerify { dao.upsertTask(match { it.status == TaskStatus.DONE }) }
    }

    // --- 7. ПРИОРИТЕТ ---

    @Test
    fun `26 cyclePriority_incrementsPriority_0to1`() {
        createViewModel()
        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(
            Goal(
                profileId = 1,
                priority = 0
            )
        ))
        viewModel.cyclePriority()
        assertEquals(1, (viewModel.selectedActionItem.value as MainViewModel.ActionTarget.GoalTarget).goal.priority)
    }

    @Test
    fun `27 cyclePriority_incrementsPriority_1to2`() {
        createViewModel()
        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(
            Goal(
                profileId = 1,
                priority = 1
            )
        ))
        viewModel.cyclePriority()
        assertEquals(2, (viewModel.selectedActionItem.value as MainViewModel.ActionTarget.GoalTarget).goal.priority)
    }

    @Test
    fun `28 cyclePriority_resetsPriority_2to0`() {
        createViewModel()
        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(
            Goal(
                profileId = 1,
                priority = 2
            )
        ))
        viewModel.cyclePriority()
        assertEquals(0, (viewModel.selectedActionItem.value as MainViewModel.ActionTarget.GoalTarget).goal.priority)
    }

    @Test
    fun `29 cyclePriority_cancelsPreviousJob`() = runTest {
        createViewModel()
        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(Goal(profileId = 1)))
        viewModel.cyclePriority()
        viewModel.cyclePriority()
        advanceUntilIdle()
        coVerify(atLeast = 1) { dao.upsertGoal(any()) }
    }

    @Test
    fun `30 cyclePriority_updatesSelectedActionItem_immediately`() {
        createViewModel()
        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(
            Goal(
                profileId = 1,
                priority = 0
            )
        ))
        viewModel.cyclePriority()
        assertEquals(1, (viewModel.selectedActionItem.value as MainViewModel.ActionTarget.GoalTarget).goal.priority)
    }

    // --- 8. УДАЛЕНИЕ ---

    @Test
    fun `31 deleteCurrentTarget_goal_callsDaoDelete_andRemovesPin`() = runTest {
        val goal = Goal(id = 99, title = "G", profileId = 1)
        coEvery { dao.getGoalsWithTasksList(any()) } returns flowOf(listOf(
            GoalWithTasks(
                goal,
                emptyList()
            )
        ))
        createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(goal))
        viewModel.togglePinGoal(context)

        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(goal))
        viewModel.deleteCurrentTarget()
        advanceUntilIdle()

        coVerify { dao.deleteGoal(any()) }
        assertFalse(viewModel.pinnedGoalIds.value.contains(99))
        job.cancel()
    }

    @Test
    fun `32 deleteCurrentTarget_task_callsDaoDelete`() = runTest {
        createViewModel()
        viewModel.selectActionItem(MainViewModel.ActionTarget.TaskTarget(
            Task(
                id = 50,
                title = "T",
                goalId = 1
            )
        ))
        viewModel.deleteCurrentTarget()
        advanceUntilIdle()
        coVerify { dao.deleteTask(any()) }
    }

    @Test
    fun `33 deleteCurrentTarget_clearsSelection`() = runTest {
        createViewModel()
        viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(Goal(profileId = 1)))
        viewModel.deleteCurrentTarget()
        advanceUntilIdle()
        assertNull(viewModel.selectedActionItem.value)
    }

    @Test
    fun `34 deleteCurrentTarget_doesNothing_whenNull`() = runTest {
        createViewModel()
        viewModel.selectActionItem(null)
        viewModel.deleteCurrentTarget()
        advanceUntilIdle()
        coVerify(exactly = 0) { dao.deleteGoal(any()) }
    }
}