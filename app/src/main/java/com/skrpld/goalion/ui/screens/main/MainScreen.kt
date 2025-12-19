package com.skrpld.goalion.ui.screens.main

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skrpld.goalion.ui.components.main.ActionFabMenu
import com.skrpld.goalion.ui.components.main.TaskDetailsDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedActionItem by viewModel.selectedActionItem.collectAsStateWithLifecycle()
    val selectedTaskForDetails by viewModel.selectedTaskForDetails.collectAsStateWithLifecycle()
    val editingId by viewModel.editingId.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ActionFabMenu(
                selectedTarget = selectedActionItem,
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
                onPriority = { viewModel.cyclePriority() }
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
                                state.items.find { it.goal.id == id }?.let {
                                    viewModel.updateGoalTitle(it.goal, title)
                                }
                            } else {
                                state.items.flatMap { it.tasks }.find { it.id == id }?.let {
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