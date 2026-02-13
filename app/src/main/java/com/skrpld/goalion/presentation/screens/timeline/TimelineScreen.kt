package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.dialogState.isOpen) {
        UniversalEditDialog(
            state = uiState.dialogState,
            onDismiss = viewModel::closeDialog,
            onSave = viewModel::onSaveDialog,
            onDelete = viewModel::onDeleteFromDialog
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openCreateGoalDialog,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TimelineHeader(
                scrollOffsetX = viewModel.scrollOffsetX
            )

            TimelineContent(
                goals = uiState.goals,
                scrollOffsetX = viewModel.scrollOffsetX,
                scrollOffsetY = viewModel.scrollOffsetY,
                onScroll = viewModel::onScroll,
                onToggleExpand = viewModel::toggleGoalExpansion,
                onEditGoal = viewModel::openEditGoalDialog,
                onAddTask = viewModel::openCreateTaskDialog,
                onEditTask = viewModel::openEditTaskDialog
            )

            if (uiState.isLoading) {
                // Loading indicator logic
            }

            uiState.errorMessage?.let { error ->
                // Error handling logic
            }
        }
    }
}