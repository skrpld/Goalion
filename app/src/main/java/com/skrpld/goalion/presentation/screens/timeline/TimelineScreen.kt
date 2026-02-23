package com.skrpld.goalion.presentation.screens.timeline

import androidx.compose.foundation.gestures.Orientation
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val currentDayWidth = if (uiState.zoomLevel == ZoomLevel.NORMAL) DAY_WIDTH_NORMAL else DAY_WIDTH_ZOOMED
    val dayWidthPx = with(density) { currentDayWidth.toPx() }
    val baseTime = getBaseTime()


    val (minScrollX, maxScrollX) = remember(dayWidthPx, screenWidthPx) {
        val daysPast = 365f
        val daysFuture = 365f * 3
        val startX = -daysPast * dayWidthPx
        val endX = daysFuture * dayWidthPx

        startX to (endX - screenWidthPx).coerceAtLeast(startX)
    }

    var maxScrollY by remember { mutableStateOf(0f) }

    val onBoundScroll: (Float, Orientation) -> Unit = { delta, orientation ->
        if (orientation == Orientation.Horizontal) {
            val newX = (viewModel.scrollOffsetX - delta).coerceIn(minScrollX, maxScrollX)
            viewModel.setScroll(newX, viewModel.scrollOffsetY)
        } else {
            val limitY = (maxScrollY - screenHeightPx + 200f).coerceAtLeast(0f)
            val newY = (viewModel.scrollOffsetY - delta).coerceIn(0f, limitY)
            viewModel.setScroll(viewModel.scrollOffsetX, newY)
        }
    }

    val jumpToDate: (Long) -> Unit = { dateMillis ->
        val daysFromBase = (dateMillis - baseTime).toFloat() / MILLIS_IN_DAY
        val targetX = (daysFromBase * dayWidthPx) - (screenWidthPx / 2)
        viewModel.setScroll(targetX.coerceIn(minScrollX, maxScrollX), viewModel.scrollOffsetY)
    }

    LaunchedEffect(uiState.scrollRequest) {
        uiState.scrollRequest?.let {
            val now = System.currentTimeMillis()
            jumpToDate(now)
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                dayWidth = currentDayWidth,
                baseTime = baseTime
            )

            TimelineContent(
                goals = uiState.goals,
                scrollOffsetX = viewModel.scrollOffsetX,
                scrollOffsetY = viewModel.scrollOffsetY,
                zoomLevel = uiState.zoomLevel,
                dayWidth = currentDayWidth,
                screenWidthPx = screenWidthPx,
                baseTime = baseTime,
                onContentHeightChanged = { height -> maxScrollY = height },
                onScroll = onBoundScroll,
                onToggleExpand = viewModel::toggleGoalExpansion,
                onEditGoal = viewModel::openEditGoalDialog,
                onAddTask = viewModel::openCreateTaskDialog,
                onEditTask = viewModel::openEditTaskDialog,
                onViewTask = viewModel::openViewTask,
                onToggleGoalStatus = viewModel::toggleGoalStatus,
                onToggleTaskStatus = viewModel::toggleTaskStatus,
                onJump = { x ->
                    val centered = x - (screenWidthPx / 2)
                    viewModel.setScroll(centered.coerceIn(minScrollX, maxScrollX), viewModel.scrollOffsetY)
                }
            )
        }
    }
}