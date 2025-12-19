package com.skrpld.goalion.ui.screens.main

import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.data.models.Goal
import com.skrpld.goalion.data.models.Task
import com.skrpld.goalion.ui.components.main.GoalCard

@Composable
fun GoalsList(
    headers: List<GoalListItem.GoalHeader>,
    editingId: String?,
    selectedActionItem: MainViewModel.ActionTarget?,
    onGoalToggle: (Int) -> Unit,
    onGoalLongClick: (Goal) -> Unit,
    onGoalDoubleClick: (Goal) -> Unit,
    onTaskClick: (Task) -> Unit,
    onTaskLongClick: (Task) -> Unit,
    onTaskDoubleClick: (Task) -> Unit,
    onAddTask: (Int) -> Unit,
    onTitleChange: (Int, String, Boolean) -> Unit,
    onEditDone: () -> Unit,
    onMoveGoal: (Int, Int) -> Unit,
    onMoveTask: (Int, Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = headers,
            key = { it.goal.id }
        ) { header ->
            GoalCard(
                goal = header.goal,
                tasks = header.tasks,
                isExpanded = header.isExpanded,
                isEditing = editingId == "goal_${header.goal.id}",
                selectedTarget = selectedActionItem,
                editingTaskId = editingId,
                onToggle = { onGoalToggle(header.goal.id) },
                onLongClick = { onGoalLongClick(header.goal) },
                onDoubleClick = { onGoalDoubleClick(header.goal) },
                onTaskClick = onTaskClick,
                onTaskLongClick = onTaskLongClick,
                onTaskDoubleClick = onTaskDoubleClick,
                onAddSubTask = { onAddTask(header.goal.id) },
                onTitleChange = { onTitleChange(header.goal.id, it, true) },
                onTaskTitleChange = { id, title -> onTitleChange(id, title, false) },
                onEditDone = onEditDone,
                onMoveTask = { from, to -> onMoveTask(header.goal.id, from, to) },
                modifier = Modifier.animateItem(
                    placementSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = 300f
                    )
                )
            )
        }
    }
}