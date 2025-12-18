package com.skrpld.goalion.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.data.models.Task
import com.skrpld.goalion.ui.components.main.GoalCard
import com.skrpld.goalion.ui.components.main.TaskCard

@Composable
fun GoalsList(
    items: List<GoalListItem>,
    onGoalClick: (Int) -> Unit,
    onTaskClick: (Task) -> Unit,
    onAddTask: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = items,
            key = { item ->
                when (item) {
                    is GoalListItem.GoalHeader -> "goal_${item.goal.id}"
                    is GoalListItem.TaskItem -> "task_${item.task.id}"
                }
            },
            contentType = { item ->
                when (item) {
                    is GoalListItem.GoalHeader -> "header"
                    is GoalListItem.TaskItem -> "task"
                }
            }
        ) { item ->
            when (item) {
                is GoalListItem.GoalHeader -> {
                    GoalCard(
                        goal = item.goal,
                        isExpanded = item.isExpanded,
                        onToggle = { onGoalClick(item.goal.id) },
                        onAddSubTask = { onAddTask(item.goal.id) },
                        modifier = Modifier.animateItem()
                    )
                }
                is GoalListItem.TaskItem -> {
                    TaskCard(
                        task = item.task,
                        onClick = { onTaskClick(item.task) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}