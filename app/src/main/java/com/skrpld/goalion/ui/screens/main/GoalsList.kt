package com.skrpld.goalion.ui.screens.main

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
    items: List<GoalListItem>,
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
    onEditDone: () -> Unit
) {
    val groupedItems = remember(items) {
        val list = mutableListOf<Pair<GoalListItem.GoalHeader, List<Task>>>()
        var currentGoal: GoalListItem.GoalHeader? = null
        var currentTasks = mutableListOf<Task>()

        items.forEach { item ->
            when (item) {
                is GoalListItem.GoalHeader -> {
                    if (currentGoal != null) list.add(currentGoal to currentTasks)
                    currentGoal = item
                    currentTasks = mutableListOf()
                }
                is GoalListItem.TaskItem -> currentTasks.add(item.task)
            }
        }
        currentGoal?.let { list.add(it to currentTasks) }
        list
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = groupedItems, key = { (header, _) -> "goal_${header.goal.id}" }) { (header, tasks) ->
            GoalCard(
                goal = header.goal,
                tasks = tasks,
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
                modifier = Modifier.animateItem()
            )
        }
    }
}