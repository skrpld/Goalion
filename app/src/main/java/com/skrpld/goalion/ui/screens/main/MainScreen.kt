package com.skrpld.goalion.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skrpld.goalion.ui.components.main.GoalItemCard
import com.skrpld.goalion.ui.components.main.TopBar
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    Scaffold(
        topBar = { TopBar(viewModel) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddGoalClick() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            itemsIndexed(state.goals, key = { _, item -> item.goal.id }) { index, goalWithTasks ->

                Box {
                    GoalItemCard(
                        item = goalWithTasks,
                        startEditing = state.goalIdToEdit == goalWithTasks.goal.id,
                        taskIdToEdit = state.taskIdToEdit,
                        onAddTaskClick = { viewModel.onAddTaskClick(goalWithTasks.goal.id) },
                        onEditGoalTitle = { newTitle -> viewModel.onGoalTitleChanged(goalWithTasks.goal, newTitle) },
                        onGoalEditStarted = { viewModel.onGoalEditStarted() },
                        onEditTaskTitle = { task, newTitle -> viewModel.onTaskTitleChanged(task, newTitle) },
                        onTaskEditStarted = { viewModel.onTaskEditStarted() },
                        onTaskStatusClick = { task -> viewModel.onTaskStatusChange(task) },
                        onTaskPriorityClick = { task -> viewModel.onTaskPriorityChange(task) },
                        onGoalStatusClick = { viewModel.onGoalStatusChange(goalWithTasks.goal.id, goalWithTasks.goal.status) },
                        onGoalPriorityClick = { viewModel.onGoalPriorityChange(goalWithTasks.goal.id, goalWithTasks.goal.priority) }
                    )
                }
            }
        }
    }
}