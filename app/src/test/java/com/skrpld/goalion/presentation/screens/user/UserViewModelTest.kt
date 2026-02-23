package com.skrpld.goalion.presentation.screens.user

import com.skrpld.goalion.domain.model.Profile
import com.skrpld.goalion.domain.model.User
import com.skrpld.goalion.domain.repositories.AuthRepository
import com.skrpld.goalion.domain.repositories.ProfileRepository
import com.skrpld.goalion.domain.repositories.UserRepository
import com.skrpld.goalion.domain.usecases.ChangePasswordUseCase
import com.skrpld.goalion.domain.usecases.CreateProfileUseCase
import com.skrpld.goalion.domain.usecases.GetProfilesUseCases
import com.skrpld.goalion.domain.usecases.GetUserUseCase
import com.skrpld.goalion.domain.usecases.LogoutUseCase
import com.skrpld.goalion.domain.usecases.ProfileInteractors
import com.skrpld.goalion.domain.usecases.SignInUseCase
import com.skrpld.goalion.domain.usecases.SignUpUseCase
import com.skrpld.goalion.domain.usecases.SyncProfilesUseCase
import com.skrpld.goalion.domain.usecases.UpdateUserUseCase
import com.skrpld.goalion.domain.usecases.UserInteractors
import com.skrpld.goalion.presentation.screens.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var profileRepository: ProfileRepository

    private lateinit var userInteractors: UserInteractors
    private lateinit var profileInteractors: ProfileInteractors
    private lateinit var viewModel: UserViewModel

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

        userInteractors = UserInteractors(
            signUp = SignUpUseCase(authRepository, userRepository),
            signIn = SignInUseCase(authRepository),
            logout = LogoutUseCase(authRepository),
            reauthenticate = mockk(relaxed = true),
            changePassword = ChangePasswordUseCase(authRepository),
            getUser = GetUserUseCase(authRepository, userRepository),
            updateUser = UpdateUserUseCase(userRepository, authRepository),
            deleteUser = mockk(relaxed = true)
        )

        profileInteractors = ProfileInteractors(
            getProfiles = GetProfilesUseCases(profileRepository),
            create = CreateProfileUseCase(profileRepository),
            update = mockk(relaxed = true),
            delete = mockk(relaxed = true),
            sync = SyncProfilesUseCase(profileRepository)
        )
    }

    @After
    fun teardown() {
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `test_01_init_coldStart`() = runTest {
        val user = User(id = "user1", name = "Test", email = "test@mail.com")
        coEvery { authRepository.getCurrentUser() } returns "user1"
        coEvery { userRepository.getUser("user1") } returns user
        coEvery { profileRepository.getProfilesByUser("user1") } returns listOf(mockk())

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(user, state.user)
        assertTrue(state.profiles.isNotEmpty())
    }

    @Test
    fun `test_02_init_authorized`() = runTest {
        val user = User(id = "user1", name = "Test", email = "test@mail.com")
        coEvery { authRepository.getCurrentUser() } returns "user1"
        coEvery { userRepository.getUser("user1") } returns user
        coEvery { profileRepository.getProfilesByUser("user1") } returns listOf(mockk())

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(user, state.user)
        assertTrue(state.profiles.isNotEmpty())
    }

    @Test
    fun `test_03_toggleAuthMode`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        val initialMode = viewModel.state.value.isLoginMode
        viewModel.toggleAuthMode()
        advanceUntilIdle()

        assertEquals(!initialMode, viewModel.state.value.isLoginMode)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `test_04_signUp_success`() = runTest {
        val user = User(id = "1", name = "ValidName123", email = "test@test.com")
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { userRepository.isNameTaken(any()) } returns false
        coEvery { userRepository.isEmailTaken(any()) } returns false
        coEvery { authRepository.signUp(any(), any(), any()) } returns Result.success(user)

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        if (viewModel.state.value.isLoginMode) {
            viewModel.toggleAuthMode()
        }

        viewModel.nameInput.value = "ValidName123"
        viewModel.emailInput.value = "test@test.com"
        viewModel.passwordInput.value = "123456"
        viewModel.submitAuth()
        advanceUntilIdle()

        assertEquals(user, viewModel.state.value.user)
        assertEquals("", viewModel.passwordInput.value)
    }

    @Test
    fun `test_05_signUp_validationError`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        if (viewModel.state.value.isLoginMode) {
            viewModel.toggleAuthMode()
        }

        viewModel.nameInput.value = "ValidName123"
        viewModel.emailInput.value = "invalid_email"
        viewModel.passwordInput.value = "123"
        viewModel.submitAuth()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
        coVerify(exactly = 0) { authRepository.signUp(any(), any(), any()) }
    }

    @Test
    fun `test_06_signUp_conflict`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { userRepository.isNameTaken(any()) } returns true

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        if (viewModel.state.value.isLoginMode) {
            viewModel.toggleAuthMode()
        }

        viewModel.nameInput.value = "ValidName123"
        viewModel.emailInput.value = "test@test.com"
        viewModel.passwordInput.value = "123456"
        viewModel.submitAuth()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error?.contains("already taken") == true)
    }

    @Test
    fun `test_07_login_success`() = runTest {
        val user = User(id = "1", name = "ValidName123", email = "test@test.com")
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { authRepository.signIn(any(), any()) } returns Result.success(user)
        coEvery { profileRepository.getProfilesByUser("1") } returns emptyList()

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        if (!viewModel.state.value.isLoginMode) {
            viewModel.toggleAuthMode()
        }

        viewModel.emailInput.value = "test@test.com"
        viewModel.passwordInput.value = "123456"
        viewModel.submitAuth()
        advanceUntilIdle()

        coVerify { profileRepository.getProfilesByUser(user.id) }
        assertEquals(user, viewModel.state.value.user)
    }

    @Test
    fun `test_08_login_error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { authRepository.signIn(any(), any()) } returns Result.failure(Exception("Wrong pass"))

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        if (!viewModel.state.value.isLoginMode) {
            viewModel.toggleAuthMode()
        }

        viewModel.emailInput.value = "test@test.com"
        viewModel.passwordInput.value = "123456"
        viewModel.submitAuth()
        advanceUntilIdle()

        assertEquals("Wrong pass", viewModel.state.value.error)
    }

    @Test
    fun `test_09_logout`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        viewModel.nameInput.value = "Test"
        viewModel.logout()
        advanceUntilIdle()

        coVerify { authRepository.logout() }
        assertNull(viewModel.state.value.user)
        assertEquals("", viewModel.nameInput.value)
    }

    @Test
    fun `test_10_sync_success`() = runTest {
        val user = User(id = "1", name = "Test", email = "test@mail.com")
        coEvery { authRepository.getCurrentUser() } returns "1"
        coEvery { userRepository.getUser("1") } returns user

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        viewModel.syncAll()
        advanceUntilIdle()

        coVerify { profileRepository.sync("1") }
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `test_11_sync_network_failure`() = runTest {
        val user = User(id = "1", name = "Test", email = "test@mail.com")
        coEvery { authRepository.getCurrentUser() } returns "1"
        coEvery { userRepository.getUser("1") } returns user
        coEvery { profileRepository.sync(any()) } throws Exception("Network Error")

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        viewModel.syncAll()
        advanceUntilIdle()

        assertEquals("Sync failed: Network Error", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `test_12_editUser_success`() = runTest {
        val user = User(id = "1", name = "Test", email = "test@mail.com")
        coEvery { authRepository.getCurrentUser() } returns "1"
        coEvery { userRepository.getUser("1") } returns user

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        viewModel.showEditDialog()
        viewModel.nameInput.value = "NewValid123"
        viewModel.saveUserData()
        advanceUntilIdle()

        coVerify { authRepository.upsertUser(match { it.name == "NewValid123" }) }
        assertFalse(viewModel.state.value.showEditUserDialog)
    }

    @Test
    fun `test_13_changePassword_success`() = runTest {
        val user = User(id = "1", name = "Test", email = "test@mail.com")
        coEvery { authRepository.getCurrentUser() } returns "1"
        coEvery { userRepository.getUser("1") } returns user
        coEvery { authRepository.changePassword(any(), any()) } returns Result.success(Unit)

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        viewModel.switchToChangePassword()
        viewModel.currentPassInput.value = "oldPass123"
        viewModel.newPassInput.value = "newPass123"
        viewModel.confirmPassInput.value = "newPass123"
        viewModel.submitChangePassword()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showChangePasswordDialog)
        assertEquals("Password changed successfully", viewModel.state.value.error)
    }

    @Test
    fun `test_14_changePassword_error`() = runTest {
        val user = User(id = "1", name = "Test", email = "test@mail.com")
        coEvery { authRepository.getCurrentUser() } returns "1"
        coEvery { userRepository.getUser("1") } returns user

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        viewModel.switchToChangePassword()
        viewModel.currentPassInput.value = "oldPass"
        viewModel.newPassInput.value = "new" // Too short!
        viewModel.confirmPassInput.value = "new"
        viewModel.submitChangePassword()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `test_15_createProfile_success`() = runTest {
        val user = User(id = "1", name = "Test", email = "test@mail.com")
        coEvery { authRepository.getCurrentUser() } returns "1"
        coEvery { userRepository.getUser("1") } returns user

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        viewModel.showCreateProfile()
        viewModel.newProfileTitleInput.value = "Work"
        viewModel.createProfile()
        advanceUntilIdle()

        coVerify { profileRepository.upsert(match { it.title == "Work" }) }
        assertFalse(viewModel.state.value.showCreateProfileDialog)
    }

    @Test
    fun `test_16_createProfile_emptyTitle`() = runTest {
        val user = User(id = "1", name = "Test", email = "test@mail.com")
        coEvery { authRepository.getCurrentUser() } returns "1"
        coEvery { userRepository.getUser("1") } returns user

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        viewModel.showCreateProfile()
        viewModel.newProfileTitleInput.value = ""
        viewModel.createProfile()
        advanceUntilIdle()

        assertEquals("Title cannot be empty", viewModel.state.value.error)
        assertTrue(viewModel.state.value.showCreateProfileDialog)
    }

    @Test
    fun `test_17_loadProfiles_success`() = runTest {
        val user = User(id = "1", name = "Test", email = "test@mail.com")
        val profiles = listOf(Profile(id = "p1", userId = "1", title = "P1", description = ""))
        coEvery { authRepository.getCurrentUser() } returns "1"
        coEvery { userRepository.getUser("1") } returns user
        coEvery { profileRepository.getProfilesByUser("1") } returns profiles

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        assertEquals(profiles, viewModel.state.value.profiles)
    }

    @Test
    fun `test_18_snackbar_error_clear`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { authRepository.signIn(any(), any()) } returns Result.failure(Exception("Error1"))

        viewModel = UserViewModel(userInteractors, profileInteractors)
        advanceUntilIdle()

        if (!viewModel.state.value.isLoginMode) {
            viewModel.toggleAuthMode()
        }

        viewModel.emailInput.value = "test@test.com"
        viewModel.passwordInput.value = "123456"
        viewModel.submitAuth()
        advanceUntilIdle()

        assertEquals("Error1", viewModel.state.value.error)

        viewModel.toggleAuthMode()
        advanceUntilIdle()
        assertNull(viewModel.state.value.error)
    }
}