package com.skrpld.goalion.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel


@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.addGoal("New goal", (uiState as MainViewModel.MainUiState.Success).profileId)
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MainViewModel.MainUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is MainViewModel.MainUiState.Empty -> Text("List is empty", Modifier.align(Alignment.Center))
                is MainViewModel.MainUiState.Success -> {
                    GoalsList(
                        items = state.items,
                        onGoalClick = { viewModel.toggleGoalExpanded(it) },
                        onTaskClick = { viewModel.showTaskDetails(it) },
                        onAddTask = { goalId -> viewModel.addTask(goalId, "New task") }
                    )
                }
            }
        }
    }
}