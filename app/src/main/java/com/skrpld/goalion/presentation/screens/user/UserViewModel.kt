package com.skrpld.goalion.presentation.screens.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skrpld.goalion.domain.model.Profile
import com.skrpld.goalion.domain.model.User
import com.skrpld.goalion.domain.usecases.ProfileInteractors
import com.skrpld.goalion.domain.usecases.UserInteractors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserScreenState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val profiles: List<Profile> = emptyList(),
    val error: String? = null,

    val isLoginMode: Boolean = false,

    val showEditUserDialog: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val showCreateProfileDialog: Boolean = false
)

class UserViewModel(
    private val userInteractors: UserInteractors,
    private val profileInteractors: ProfileInteractors
) : ViewModel() {

    private val _state = MutableStateFlow(UserScreenState())
    val state: StateFlow<UserScreenState> = _state.asStateFlow()

    // Поля ввода (храним в VM, чтобы переживали поворот экрана)
    var nameInput = MutableStateFlow("")
    var emailInput = MutableStateFlow("")
    var passwordInput = MutableStateFlow("")

    // Поля для смены пароля
    var currentPassInput = MutableStateFlow("")
    var newPassInput = MutableStateFlow("")
    var confirmPassInput = MutableStateFlow("")

    // Поля для создания профиля
    var newProfileTitleInput = MutableStateFlow("")
    var newProfileDescInput = MutableStateFlow("")

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val user = userInteractors.getUser()
            if (user != null) {
                onUserLoggedIn(user)
            } else {
                _state.update { it.copy(isLoading = false, user = null) }
            }
        }
    }

    private suspend fun onUserLoggedIn(user: User) {
        _state.update { it.copy(user = user, isLoading = false, error = null) }
        loadProfiles(user.id)
        // Предзаполняем поля редактирования
        nameInput.value = user.name
        emailInput.value = user.email
    }

    private suspend fun loadProfiles(userId: String) {
        try {
            val profiles = profileInteractors.getProfiles(userId)
            _state.update { it.copy(profiles = profiles) }
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to load profiles: ${e.message}") }
        }
    }

    // --- Auth Logic ---

    fun toggleAuthMode() {
        _state.update { it.copy(isLoginMode = !it.isLoginMode, error = null) }
        // Очистка полей при переключении не обязательна, но желательна
    }

    fun submitAuth() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val name = nameInput.value
            val email = emailInput.value
            val pass = passwordInput.value

            val result = if (_state.value.isLoginMode) {
                userInteractors.signIn(email, pass)
            } else {
                userInteractors.signUp(name, email, pass)
            }

            result.fold(
                onSuccess = { user ->
                    onUserLoggedIn(user)
                    // Очищаем пароль после успешного входа
                    passwordInput.value = ""
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            userInteractors.logout()
            _state.update { UserScreenState() } // Сброс состояния
            // Очистка полей
            nameInput.value = ""
            emailInput.value = ""
            passwordInput.value = ""
        }
    }

    // --- Profile & User Edit Logic ---

    fun syncAll() {
        val user = _state.value.user ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                profileInteractors.sync(user.id)
                loadProfiles(user.id)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Sync failed: ${e.message}") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // Управление диалогами
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
        viewModelScope.launch {
            try {
                userInteractors.updateUser(user.id, nameInput.value, emailInput.value)
                // Обновляем локального пользователя
                val updatedUser = userInteractors.getUser()
                if (updatedUser != null) {
                    _state.update { it.copy(user = updatedUser) }
                }
                hideEditDialog()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun submitChangePassword() {
        viewModelScope.launch {
            val result = userInteractors.changePassword(
                currentPassInput.value,
                newPassInput.value,
                confirmPassInput.value
            )
            result.fold(
                onSuccess = {
                    hideChangePasswordDialog()
                    _state.update { it.copy(error = "Password changed successfully") } // Можно использовать Toast в UI
                },
                onFailure = { e ->
                    _state.update { it.copy(error = e.message) }
                }
            )
        }
    }

    // Для создания профиля
    fun showCreateProfile() { _state.update { it.copy(showCreateProfileDialog = true) } }
    fun hideCreateProfile() { _state.update { it.copy(showCreateProfileDialog = false) } }

    fun createProfile() {
        val user = _state.value.user ?: return
        viewModelScope.launch {
            try {
                profileInteractors.create(user.id, newProfileTitleInput.value, newProfileDescInput.value)
                loadProfiles(user.id)
                hideCreateProfile()
                newProfileTitleInput.value = ""
                newProfileDescInput.value = ""
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}