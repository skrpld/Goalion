package com.skrpld.goalion.presentation.screens.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrpld.goalion.domain.model.User
import com.skrpld.goalion.domain.usecases.ProfileInteractors
import com.skrpld.goalion.domain.usecases.UserInteractors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel(
    private val userInteractors: UserInteractors,
    private val profileInteractors: ProfileInteractors
) : ViewModel() {

    private val TAG = "GoalionLog_UserVM"

    private val _state = MutableStateFlow(UserScreenState())
    val state: StateFlow<UserScreenState> = _state.asStateFlow()

    var nameInput = MutableStateFlow("")
    var emailInput = MutableStateFlow("")
    var passwordInput = MutableStateFlow("")

    var currentPassInput = MutableStateFlow("")
    var newPassInput = MutableStateFlow("")
    var confirmPassInput = MutableStateFlow("")

    var newProfileTitleInput = MutableStateFlow("")
    var newProfileDescInput = MutableStateFlow("")

    init {
        Log.d(TAG, "Init UserViewModel, checking Auth status...")
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val user = userInteractors.getUser()
            if (user != null) {
                Log.d(TAG, "[UI_STATE] User is already logged in: ${user.email} (ID: ${user.id})")
                onUserLoggedIn(user)
            } else {
                Log.d(TAG, "[UI_STATE] No user found, showing Auth screen")
                _state.update { it.copy(isLoading = false, user = null) }
            }
        }
    }

    private suspend fun onUserLoggedIn(user: User) {
        _state.update { it.copy(user = user, isLoading = false, error = null) }
        loadProfiles(user.id)
        nameInput.value = user.name
        emailInput.value = user.email
    }

    private suspend fun loadProfiles(userId: String) {
        Log.d(TAG, "Loading profiles for user: $userId")
        try {
            val profiles = profileInteractors.getProfiles(userId)
            Log.d(TAG, "[UI_STATE] Loaded ${profiles.size} profiles")
            _state.update { it.copy(profiles = profiles) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load profiles: ${e.message}", e)
            _state.update { it.copy(error = "Failed to load profiles: ${e.message}") }
        }
    }

    // --- Auth ---

    fun toggleAuthMode() {
        _state.update { it.copy(isLoginMode = !it.isLoginMode, error = null) }
        Log.d(TAG, "Toggled Auth Mode. isLoginMode = ${_state.value.isLoginMode}")
    }

    fun submitAuth() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val name = nameInput.value
            val email = emailInput.value
            val pass = passwordInput.value

            Log.d(TAG, "Submitting Auth... LoginMode: ${_state.value.isLoginMode}, Email: $email")

            val result = if (_state.value.isLoginMode) {
                userInteractors.signIn(email, pass)
            } else {
                userInteractors.signUp(name, email, pass)
            }

            result.fold(
                onSuccess = { user ->
                    Log.d(TAG, "Auth successful for user: ${user.id}")
                    onUserLoggedIn(user)
                    passwordInput.value = ""
                },
                onFailure = { error ->
                    Log.e(TAG, "Auth failed: ${error.message}", error)
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun logout() {
        Log.d(TAG, "User requested Logout")
        viewModelScope.launch {
            userInteractors.logout()
            _state.update { UserScreenState() }
            nameInput.value = ""
            emailInput.value = ""
            passwordInput.value = ""
        }
    }

    // --- Edit ---

    fun syncAll() {
        val user = _state.value.user ?: return
        Log.d(TAG, "[SYNC] User requested manual sync for User ID: ${user.id}")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                profileInteractors.sync(user.id)
                loadProfiles(user.id) // Reload after sync
                Log.d(TAG, "[SYNC] Manual sync triggered successfully")
            } catch (e: Exception) {
                Log.e(TAG, "[SYNC] Sync failed: ${e.message}", e)
                _state.update { it.copy(error = "Sync failed: ${e.message}") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showEditDialog() {
        val user = _state.value.user ?: return
        nameInput.value = user.name
        emailInput.value = user.email
        _state.update { it.copy(showEditUserDialog = true) }
    }

    fun hideEditDialog() {
        _state.update { it.copy(showEditUserDialog = false) }
    }

    fun switchToChangePassword() {
        _state.update { it.copy(showEditUserDialog = false, showChangePasswordDialog = true) }
        currentPassInput.value = ""
        newPassInput.value = ""
        confirmPassInput.value = ""
    }

    fun hideChangePasswordDialog() {
        _state.update { it.copy(showChangePasswordDialog = false) }
    }

    fun saveUserData() {
        val user = _state.value.user ?: return
        Log.d(TAG, "Saving user data. ID: ${user.id}, New Name: ${nameInput.value}")
        viewModelScope.launch {
            try {
                userInteractors.updateUser(user.id, nameInput.value, emailInput.value)
                val updatedUser = userInteractors.getUser()
                if (updatedUser != null) {
                    _state.update { it.copy(user = updatedUser) }
                    Log.d(TAG, "[UI_STATE] User data updated successfully in UI state")
                }
                hideEditDialog()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update user data: ${e.message}", e)
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun submitChangePassword() {
        Log.d(TAG, "Submitting password change...")
        viewModelScope.launch {
            val result = userInteractors.changePassword(
                currentPassInput.value,
                newPassInput.value,
                confirmPassInput.value
            )
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Password changed successfully")
                    hideChangePasswordDialog()
                    _state.update { it.copy(error = "Password changed successfully") }
                },
                onFailure = { e ->
                    Log.e(TAG, "Failed to change password: ${e.message}", e)
                    _state.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun showCreateProfile() {
        _state.update { it.copy(showCreateProfileDialog = true) }
    }

    fun hideCreateProfile() {
        _state.update { it.copy(showCreateProfileDialog = false) }
    }

    fun createProfile() {
        val user = _state.value.user ?: return
        Log.d(TAG, "Creating new profile: ${newProfileTitleInput.value} for User ID: ${user.id}")
        viewModelScope.launch {
            try {
                profileInteractors.create(
                    user.id,
                    newProfileTitleInput.value,
                    newProfileDescInput.value
                )
                loadProfiles(user.id)
                hideCreateProfile()
                newProfileTitleInput.value = ""
                newProfileDescInput.value = ""
                Log.d(TAG, "Profile created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create profile: ${e.message}", e)
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}