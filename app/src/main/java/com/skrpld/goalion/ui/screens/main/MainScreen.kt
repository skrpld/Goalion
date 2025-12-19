package com.skrpld.goalion.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                .clickable(
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
                        onEditDone = { viewModel.stopEditing() },
                        onMoveGoal = viewModel::moveGoal,
                        onMoveTask = viewModel::moveTask
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

@Composable
fun ActionFabMenu(
    selectedTarget: MainViewModel.ActionTarget?,
    onAddGoal: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onPriority: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = selectedTarget != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(onClick = onDelete, containerColor = MaterialTheme.colorScheme.errorContainer) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
                SmallFloatingActionButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
                SmallFloatingActionButton(onClick = onPriority) {
                    Icon(Icons.Default.PriorityHigh, contentDescription = null)
                }
            }
        }
        FloatingActionButton(onClick = onAddGoal) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}