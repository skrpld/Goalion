package com.skrpld.goalion.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
    onEditDone: () -> Unit,
    onMoveGoal: (Int, Int) -> Unit,
    onMoveTask: (Int, Int, Int) -> Unit
) {
    val groupedData = remember(items) {
        val result = mutableListOf<Triple<GoalListItem.GoalHeader, List<Task>, Int>>()
        var currentGoalHeader: GoalListItem.GoalHeader? = null
        var currentTasks = mutableListOf<Task>()
        var goalIndex = 0

        items.forEach { item ->
            when (item) {
                is GoalListItem.GoalHeader -> {
                    if (currentGoalHeader != null) {
                        result.add(Triple(currentGoalHeader!!, currentTasks.toList(), goalIndex++))
                    }
                    currentGoalHeader = item
                    currentTasks = mutableListOf()
                }
                is GoalListItem.TaskItem -> {
                    currentTasks.add(item.task)
                }
            }
        }
        currentGoalHeader?.let { result.add(Triple(it, currentTasks.toList(), goalIndex)) }
        result
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = groupedData,
            key = { _, triple -> "goal_${triple.first.goal.id}" }
        ) { _, (header, tasks, _) ->
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
                onMoveTask = { from, to -> onMoveTask(header.goal.id, from, to) },
                modifier = Modifier.animateItem()
            )
        }
    }
}