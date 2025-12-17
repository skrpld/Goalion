package com.skrpld.goalion.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skrpld.goalion.ui.components.main.GoalItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hello,",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.profile.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onProfileClick() }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(4.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(state.goals, key = { it.goal.id }) { goalWithTasks ->
                GoalItemCard(
                    item = goalWithTasks,
                    startEditing = state.goalIdToEdit == goalWithTasks.goal.id,
                    taskIdToEdit = state.taskIdToEdit,

                    onAddTaskClick = { viewModel.onAddTaskClick(goalWithTasks.goal.id) },
                    onEditGoalTitle = { newTitle ->
                        viewModel.onGoalTitleChanged(goalWithTasks.goal, newTitle)
                    },
                    onGoalEditStarted = { viewModel.onGoalEditStarted() },

                    onEditTaskTitle = { task, newTitle ->
                        viewModel.onTaskTitleChanged(task, newTitle)
                    },
                    onTaskEditStarted = { viewModel.onTaskEditStarted() },
                    onTaskStatusClick = { task -> viewModel.onTaskStatusChange(task) },
                    onTaskPriorityClick = { task -> viewModel.onTaskPriorityChange(task) },

                    onGoalStatusClick = {
                        viewModel.onGoalStatusChange(goalWithTasks.goal.id, goalWithTasks.goal.status)
                    },
                    onGoalPriorityClick = {
                        viewModel.onGoalPriorityChange(goalWithTasks.goal.id, goalWithTasks.goal.priority)
                    }
                )
            }
        }
    }
}