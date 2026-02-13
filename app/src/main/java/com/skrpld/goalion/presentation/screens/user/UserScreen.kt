package com.skrpld.goalion.presentation.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserScreen(
    viewModel: UserViewModel = koinViewModel(),
    onProfileClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        snackbarHost = { /* Можно добавить SnackbarHost для отображения ошибок */ }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.user == null) {
                LoggedOutContent(viewModel, state)
            } else {
                // Передаем колбэк дальше
                LoggedInContent(viewModel, state, onProfileClick)
            }

            // Отображение загрузки
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Отображение ошибки (простой вариант текстом поверх)
            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------------------
// 1. LoggedOut Mode
// ----------------------------------------------------------------
@Composable
fun LoggedOutContent(
    viewModel: UserViewModel,
    state: UserScreenState
) {
    val name by viewModel.nameInput.collectAsState()
    val email by viewModel.emailInput.collectAsState()
    val password by viewModel.passwordInput.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Центрируем контент по вертикали
    ) {
        // Заголовок
        Text(
            text = "Welcome to Goalion",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Поле Имени (скрывается при SignIn)
        if (!state.isLoginMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.nameInput.value = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                singleLine = true
            )
        }

        // Почта
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.emailInput.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            singleLine = true
        )

        // Пароль
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.passwordInput.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            singleLine = true
        )

        // Кнопка Action
        Button(
            onClick = { viewModel.submitAuth() },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = if (state.isLoginMode) "SignIn" else "SignUp")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Переключатель режима
        val infoText = if (state.isLoginMode) "Don't have an Account? " else "Already has Account? "
        val linkText = if (state.isLoginMode) "SignUp!" else "SignIn!"

        val annotatedString = buildAnnotatedString {
            append(infoText)
            pushStringAnnotation(tag = "ACTION", annotation = "switch")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                append(linkText)
            }
            pop()
        }

        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "ACTION", start = offset, end = offset)
                    .firstOrNull()?.let {
                        viewModel.toggleAuthMode()
                    }
            }
        )
    }
}

// ----------------------------------------------------------------
// 2. LoggedIn Mode
// ----------------------------------------------------------------
@Composable
fun LoggedInContent(
    viewModel: UserViewModel,
    state: UserScreenState,
    onProfileClick: (String) -> Unit
) {
    val user = state.user ?: return

    // Вся страница в LazyColumn для общего скролла
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Кнопка Sync All (верх центр) ---
        item {
            Button(
                onClick = { viewModel.syncAll() },
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text("Sync All")
            }
        }

        // --- 2. Имя + Карандаш + Email ---
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.showEditDialog() }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                }
            }
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        // --- 3. Кнопка New Profile ---
        item {
            Button(
                onClick = { viewModel.showCreateProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("New Profile")
            }
        }

        // --- 4. Список профилей ---
        items(state.profiles) { profile ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onProfileClick(profile.id) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = profile.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Отступ снизу для красоты
        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Кнопка Logout (опционально, т.к. в ТЗ её нет на экране, но UseCase есть)
        item {
            TextButton(onClick = { viewModel.logout() }) {
                Text("Logout", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // --- Диалоги ---

    if (state.showEditUserDialog) {
        EditUserDialog(viewModel)
    }

    if (state.showChangePasswordDialog) {
        ChangePasswordDialog(viewModel)
    }

    if (state.showCreateProfileDialog) {
        CreateProfileDialog(viewModel)
    }
}

// ----------------------------------------------------------------
// Dialogs
// ----------------------------------------------------------------

@Composable
fun EditUserDialog(viewModel: UserViewModel) {
    val name by viewModel.nameInput.collectAsState()
    val email by viewModel.emailInput.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.hideEditDialog() },
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.nameInput.value = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.emailInput.value = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            // Save Button (Bottom Right)
            Button(onClick = { viewModel.saveUserData() }) {
                Text("Save")
            }
        },
        dismissButton = {
            // Change Password Button (Bottom Left)
            // Используем dismissButton слот для левой кнопки
            Button(
                onClick = { viewModel.switchToChangePassword() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Change Password")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(viewModel: UserViewModel) {
    val current by viewModel.currentPassInput.collectAsState()
    val newPass by viewModel.newPassInput.collectAsState()
    val confirm by viewModel.confirmPassInput.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.hideChangePasswordDialog() },
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = current,
                    onValueChange = { viewModel.currentPassInput.value = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { viewModel.newPassInput.value = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { viewModel.confirmPassInput.value = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.submitChangePassword() }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.hideChangePasswordDialog() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateProfileDialog(viewModel: UserViewModel) {
    val title by viewModel.newProfileTitleInput.collectAsState()
    val desc by viewModel.newProfileDescInput.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.hideCreateProfile() },
        title = { Text("New Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.newProfileTitleInput.value = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { viewModel.newProfileDescInput.value = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.createProfile() }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.hideCreateProfile() }) {
                Text("Cancel")
            }
        }
    )
}