package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

val DAY_WIDTH = 48.dp
const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
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
                onScroll = { delta, orientation ->
                    viewModel.onScroll(delta, orientation)
                },
                onToggleExpand = { index ->
                    viewModel.toggleGoalExpansion(index)
                }
            )

            if (uiState.isLoading) {
                // TODO: Loading hanging
            }

            uiState.errorMessage?.let { error ->
                // TODO: Error hanging (snack bar or something)
            }
        }
    }
}