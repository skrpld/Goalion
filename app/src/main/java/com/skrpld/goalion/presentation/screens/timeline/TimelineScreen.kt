package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skrpld.goalion.presentation.screens.timeline.TimelineDateUtils.getBaseTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val currentDayWidth = if (uiState.zoomLevel == ZoomLevel.NORMAL) DAY_WIDTH_NORMAL else DAY_WIDTH_ZOOMED
    val dayWidthPx = with(density) { currentDayWidth.toPx() }

    LaunchedEffect(uiState.scrollRequest) {
        uiState.scrollRequest?.let {
            val baseTime = getBaseTime()
            val daysFromBase = (System.currentTimeMillis() - baseTime).toFloat() / MILLIS_IN_DAY
            val screenCenter = with(density) { screenWidth.toPx() / 2 }
            val newX = (daysFromBase * dayWidthPx) - screenCenter + (dayWidthPx / 2)
            viewModel.setScroll(newX, viewModel.scrollOffsetY)
            viewModel.consumeScrollRequest()
        }
    }

    if (uiState.dialogState.isOpen) {
        EditBottomSheet(
            state = uiState.dialogState,
            onDismiss = viewModel::closeDialog,
            onSave = viewModel::onSaveDialog,
            onDelete = viewModel::onDeleteFromDialog
        )
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallFloatingActionButton(
                    onClick = viewModel::toggleShowCompleted,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        if (uiState.showCompleted) Icons.Default.VisibilityOff else Icons.Default.Archive,
                        contentDescription = "Archive"
                    )
                }

                SmallFloatingActionButton(
                    onClick = viewModel::cycleZoomLevel,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        if (uiState.zoomLevel == ZoomLevel.NORMAL) Icons.Default.ZoomIn else Icons.Default.ZoomOut,
                        contentDescription = "Zoom"
                    )
                }

                FloatingActionButton(
                    onClick = viewModel::openCreateGoalDialog,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TimelineHeader(
                scrollOffsetX = viewModel.scrollOffsetX,
                zoomLevel = uiState.zoomLevel,
                dayWidth = currentDayWidth
            )

            TimelineContent(
                goals = uiState.goals,
                scrollOffsetX = viewModel.scrollOffsetX,
                scrollOffsetY = viewModel.scrollOffsetY,
                zoomLevel = uiState.zoomLevel,
                dayWidth = currentDayWidth,
                onScroll = viewModel::onScroll,
                onToggleExpand = viewModel::toggleGoalExpansion,
                onEditGoal = viewModel::openEditGoalDialog,
                onAddTask = viewModel::openCreateTaskDialog,
                onEditTask = viewModel::openEditTaskDialog,
                onViewTask = viewModel::openViewTask,
                onToggleGoalStatus = viewModel::toggleGoalStatus,
                onToggleTaskStatus = viewModel::toggleTaskStatus
            )
        }
    }
}