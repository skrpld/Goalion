package com.skrpld.goalion.domain.usecases

import com.skrpld.goalion.domain.model.GoalWithTasks
import com.skrpld.goalion.domain.model.User
import com.skrpld.goalion.domain.repositories.AuthRepository
import com.skrpld.goalion.domain.repositories.GoalRepository
import com.skrpld.goalion.domain.repositories.ProfileRepository
import com.skrpld.goalion.domain.repositories.TaskRepository
import com.skrpld.goalion.domain.repositories.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DomainUseCasesTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var profileRepository: ProfileRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.w(any(), any<String>(), any()) } returns 0

        authRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        profileRepository = mockk(relaxed = true)
        goalRepository = mockk(relaxed = true)
        taskRepository = mockk(relaxed = true)
    }

    @After
    fun teardown() {
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `test_01_signUp_success`() = runTest {
        val useCase = SignUpUseCase(authRepository, userRepository)
        val user = User("1", "ValidName123", "test@test.com")
        coEvery { userRepository.isNameTaken(any()) } returns false
        coEvery { userRepository.isEmailTaken(any()) } returns false
        coEvery { authRepository.signUp(any(), any(), any()) } returns Result.success(user)

        val result = useCase("ValidName123", "test@test.com", "password123")
        assertTrue(result.isSuccess)
        coVerify { authRepository.signUp("ValidName123", "test@test.com", "password123") }
    }

    @Test
    fun `test_02_signUp_validationError`() = runTest {
        val useCase = SignUpUseCase(authRepository, userRepository)
        val result = useCase("ValidName123", "invalid_email", "123")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { authRepository.signUp(any(), any(), any()) }
    }

    @Test
    fun `test_03_signIn_success`() = runTest {
        val useCase = SignInUseCase(authRepository)
        coEvery { authRepository.signIn(any(), any()) } returns Result.success(User("1", "N", "test@mail.com"))

        val result = useCase("test@test.com", "123456")
        assertTrue(result.isSuccess)
        coVerify { authRepository.signIn("test@test.com", "123456") }
    }

    @Test
    fun `test_04_getUser_localOrNetwork`() = runTest {
        val useCase = GetUserUseCase(authRepository, userRepository)
        coEvery { authRepository.getCurrentUser() } returns "user1"

        val localUser = User("user1", "Local", "a@a.com")
        coEvery { userRepository.getUser("user1") } returns localUser
        var result = useCase()
        assertEquals(localUser, result)
        coVerify(exactly = 0) { userRepository.fetchUserFromRemote(any()) }

        coEvery { userRepository.getUser("user1") } returns null
        val remoteUser = User("user1", "Remote", "b@b.com")
        coEvery { userRepository.fetchUserFromRemote("user1") } returns remoteUser
        result = useCase()
        assertEquals(remoteUser, result)
    }

    @Test
    fun `test_05_updateUser_conflictValidation`() = runTest {
        val useCase = UpdateUserUseCase(userRepository, authRepository)
        coEvery { userRepository.getUser("1") } returns User("1", "OldName", "old@mail.com")
        coEvery { userRepository.isNameTaken("NewName123") } returns true

        try {
            useCase("1", "NewName123", "old@mail.com")
            fail("Exception was expected")
        } catch (e: Exception) {
            assertTrue(e is IllegalArgumentException)
            assertEquals("Username 'NewName123' is already taken", e.message)
        }
        coVerify(exactly = 0) { authRepository.upsertUser(any()) }
    }

    @Test
    fun `test_06_changePassword_mismatch`() = runTest {
        val useCase = ChangePasswordUseCase(authRepository)
        val result = useCase("oldPass", "newPass123", "mismatchPass")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("New passwords do not match", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test_07_createProfile_success`() = runTest {
        val useCase = CreateProfileUseCase(profileRepository)
        useCase("userId", "Work", "Desc")
        coVerify { profileRepository.upsert(match { it.title == "Work" && it.userId == "userId" }) }
    }

    @Test
    fun `test_08_getGoals_flow`() = runTest {
        val useCase = GetGoalsWithTasksUseCase(goalRepository)
        val flowMock = flowOf<List<GoalWithTasks>>()
        every { goalRepository.getGoalsWithTasks("p1") } returns flowMock

        val result = useCase("p1")
        assertEquals(flowMock, result)
    }

    @Test
    fun `test_09_createGoal_genId`() = runTest {
        val useCase = CreateGoalUseCase(goalRepository)
        useCase("p1", "GoalTitle", "Desc")
        coVerify { goalRepository.upsert(match { it.title == "GoalTitle" && it.id.isNotEmpty() }) }
    }

    @Test
    fun `test_10_updateGoal_partial`() = runTest {
        val useCase = UpdateGoalUseCase(goalRepository)
        useCase("g1", "p1", "NewTitle", "Desc", false, 1, 0, 0L, 0L)
        coVerify { goalRepository.upsert(match { it.id == "g1" && it.title == "NewTitle" }) }
    }

    @Test
    fun `test_11_deleteGoal_cascade`() = runTest {
        val useCase = DeleteGoalUseCase(goalRepository)
        useCase("g1")
        coVerify { goalRepository.delete("g1") }
    }

    @Test
    fun `test_12_createTask_bind`() = runTest {
        val useCase = CreateTaskUseCase(taskRepository)
        useCase("g1", "Task", "Desc")
        coVerify { taskRepository.upsert(match { it.goalId == "g1" && it.title == "Task" }) }
    }

    @Test
    fun `test_13_updateTaskStatus_change`() = runTest {
        val useCase = UpdateTaskStatusUseCase(taskRepository)
        useCase("t1", true)
        coVerify { taskRepository.updateStatus("t1", true) }
    }

    @Test
    fun `test_14_syncGoal_delegate`() = runTest {
        val useCase = SyncGoalUseCase(goalRepository)
        useCase("p1")
        coVerify { goalRepository.sync("p1") }
    }
}