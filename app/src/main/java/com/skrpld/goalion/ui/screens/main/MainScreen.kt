package com.skrpld.goalion.ui.screens.main

import android.Manifest
import android.os.Build
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.skrpld.goalion.ui.components.main.ActionFabMenu
import com.skrpld.goalion.ui.components.main.GoalsList
import com.skrpld.goalion.ui.components.main.TaskDetailsDialog
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedActionItem by viewModel.selectedActionItem.collectAsStateWithLifecycle()
    val selectedTaskForDetails by viewModel.selectedTaskForDetails.collectAsStateWithLifecycle()
    val editingId by viewModel.editingId.collectAsStateWithLifecycle()
    val pinnedGoalIds by viewModel.pinnedGoalIds.collectAsStateWithLifecycle()

    val currentContext = LocalContext.current

    // Состояние разрешения (только для Android 13+)
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    Scaffold(
        floatingActionButton = {
            ActionFabMenu(
                selectedTarget = selectedActionItem,
                pinnedGoalIds = pinnedGoalIds,
                onAddGoal = {
                    when (val state = uiState) {
                        is MainViewModel.MainUiState.Success -> viewModel.addGoal(state.profileId)
                        is MainViewModel.MainUiState.Empty -> viewModel.addGoal(state.profileId)
                        else -> {}
                    }
                },
                onDelete = { viewModel.deleteCurrentTarget() },
                onEdit = {
                    selectedActionItem?.let { target ->
                        val isGoal = target is MainViewModel.ActionTarget.GoalTarget
                        viewModel.startEditing(target.id, isGoal)
                    }
                },
                onPriority = { viewModel.cyclePriority() },
                onPin = {
                    // Логика проверки разрешения перед пином
                    if (notificationPermissionState != null && !notificationPermissionState.status.isGranted) {
                        notificationPermissionState.launchPermissionRequest()
                    } else {
                        viewModel.togglePinGoal(currentContext)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { viewModel.selectActionItem(null) }
                )
        ) {
            when (val state = uiState) {
                is MainViewModel.MainUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is MainViewModel.MainUiState.Empty -> {
                    Text("List is empty", Modifier.align(Alignment.Center))
                }
                is MainViewModel.MainUiState.Success -> {
                    GoalsList(
                        headers = state.items,
                        editingId = editingId,
                        selectedActionItem = selectedActionItem,
                        onGoalToggle = { viewModel.toggleGoalExpanded(it) },
                        onGoalLongClick = { viewModel.selectActionItem(MainViewModel.ActionTarget.GoalTarget(it)) },
                        onGoalDoubleClick = { viewModel.toggleStatus(MainViewModel.ActionTarget.GoalTarget(it)) },
                        onTaskClick = { viewModel.showTaskDetails(it) },
                        onTaskLongClick = { viewModel.selectActionItem(MainViewModel.ActionTarget.TaskTarget(it)) },
                        onTaskDoubleClick = { viewModel.toggleStatus(MainViewModel.ActionTarget.TaskTarget(it)) },
                        onAddTask = { goalId -> viewModel.addTask(goalId) },
                        onTitleChange = { id, title, isGoal ->
                            if (isGoal) {
                                state.items.firstOrNull { it.goal.id == id }?.let {
                                    viewModel.updateGoalTitle(it.goal, title)
                                }
                            } else {
                                state.items.flatMap { it.tasks }.firstOrNull { it.id == id }?.let {
                                    viewModel.updateTaskTitle(it, title)
                                }
                            }
                        },
                        onEditDone = { viewModel.stopEditing() }
                    )
                }
            }
        }

        selectedTaskForDetails?.let { task ->
            TaskDetailsDialog(
                task = task,
                onDismiss = { viewModel.showTaskDetails(null) },
                onSaveDescription = { viewModel.updateTaskDescription(task, it) }
            )
        }
    }
}