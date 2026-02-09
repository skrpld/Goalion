package com.skrpld.goalion.ui.screens.user

import androidx.lifecycle.ViewModel
import com.skrpld.goalion.domain.entities.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel() : ViewModel() {
    val authState: StateFlow<AuthState> = MutableStateFlow(AuthState.LoggedOut)
}