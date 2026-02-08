package com.skrpld.goalion.ui.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = koinViewModel()
) {
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
                // Теперь viewModel.goals возвращает List<TimelineGoalItem>
                goals = viewModel.goals,
                scrollOffsetX = viewModel.scrollOffsetX,
                scrollOffsetY = viewModel.scrollOffsetY,
                onScroll = { delta, orientation ->
                    viewModel.onScroll(delta, orientation)
                },
                onToggleExpand = { index ->
                    viewModel.toggleGoalExpansion(index)
                }
            )
        }
    }
}