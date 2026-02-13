package com.skrpld.goalion.presentation.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserScreen(
    viewModel: UserViewModel = koinViewModel(),
    onProfileClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (state.user == null) {
                LoggedOutContent(
                    state = state,
                    nameInput = viewModel.nameInput.collectAsState().value,
                    emailInput = viewModel.emailInput.collectAsState().value,
                    passwordInput = viewModel.passwordInput.collectAsState().value,
                    onNameChange = { viewModel.nameInput.value = it },
                    onEmailChange = { viewModel.emailInput.value = it },
                    onPasswordChange = { viewModel.passwordInput.value = it },
                    onAuthSubmit = viewModel::submitAuth,
                    onToggleMode = viewModel::toggleAuthMode
                )
            } else {
                LoggedInContent(
                    state = state,
                    onSyncAll = viewModel::syncAll,
                    onLogout = viewModel::logout,
                    onEditClick = viewModel::showEditDialog,
                    onCreateProfileClick = viewModel::showCreateProfile,
                    onProfileClick = onProfileClick
                )
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (state.showEditUserDialog) {
        EditUserDialog(
            name = viewModel.nameInput.collectAsState().value,
            email = viewModel.emailInput.collectAsState().value,
            onNameChange = { viewModel.nameInput.value = it },
            onEmailChange = { viewModel.emailInput.value = it },
            onSave = viewModel::saveUserData,
            onDismiss = viewModel::hideEditDialog,
            onSwitchToPassword = viewModel::switchToChangePassword
        )
    }

    if (state.showChangePasswordDialog) {
        ChangePasswordDialog(
            currentPass = viewModel.currentPassInput.collectAsState().value,
            newPass = viewModel.newPassInput.collectAsState().value,
            confirmPass = viewModel.confirmPassInput.collectAsState().value,
            onCurrentChange = { viewModel.currentPassInput.value = it },
            onNewChange = { viewModel.newPassInput.value = it },
            onConfirmChange = { viewModel.confirmPassInput.value = it },
            onConfirm = viewModel::submitChangePassword,
            onDismiss = viewModel::hideChangePasswordDialog
        )
    }

    if (state.showCreateProfileDialog) {
        CreateProfileDialog(
            title = viewModel.newProfileTitleInput.collectAsState().value,
            description = viewModel.newProfileDescInput.collectAsState().value,
            onTitleChange = { viewModel.newProfileTitleInput.value = it },
            onDescriptionChange = { viewModel.newProfileDescInput.value = it },
            onConfirm = viewModel::createProfile,
            onDismiss = viewModel::hideCreateProfile
        )
    }
}