package com.skrpld.goalion.presentation.screens.user

import com.skrpld.goalion.domain.model.Profile
import com.skrpld.goalion.domain.model.User

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