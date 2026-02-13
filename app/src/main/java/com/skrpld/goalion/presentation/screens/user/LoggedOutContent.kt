package com.skrpld.goalion.presentation.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoggedOutContent(
    state: UserScreenState,
    nameInput: String,
    emailInput: String,
    passwordInput: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAuthSubmit: () -> Unit,
    onToggleMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Goalion",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (!state.isLoginMode) {
            OutlinedTextField(
                value = nameInput,
                onValueChange = onNameChange,
                label = { Text("Name", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true
            )
        }

        OutlinedTextField(
            value = emailInput,
            onValueChange = onEmailChange,
            label = { Text("Email", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true
        )

        OutlinedTextField(
            value = passwordInput,
            onValueChange = onPasswordChange,
            label = { Text("Password", style = MaterialTheme.typography.labelSmall) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true
        )

        Button(
            onClick = onAuthSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = if (state.isLoginMode) "SignIn" else "SignUp",
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val infoText = if (state.isLoginMode) "Don't have an Account? " else "Already has Account? "
        val linkText = if (state.isLoginMode) "SignUp!" else "SignIn!"

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = infoText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = linkText,
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.clickable { onToggleMode() }
            )
        }
    }
}