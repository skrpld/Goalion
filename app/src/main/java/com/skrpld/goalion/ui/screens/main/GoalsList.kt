package com.skrpld.goalion.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.data.models.Task
import com.skrpld.goalion.ui.components.main.GoalCard
import com.skrpld.goalion.ui.theme.GoalionTheme

@Composable
fun GoalsList(
    items: List<GoalListItem>,
    onGoalClick: (Int) -> Unit,
    onTaskClick: (Task) -> Unit,
    onAddTask: (Int) -> Unit
) {
    val groupedItems = remember(items) {
        val list = mutableListOf<Pair<GoalListItem.GoalHeader, List<Task>>>()
        var currentGoal: GoalListItem.GoalHeader? = null
        var currentTasks = mutableListOf<Task>()

        items.forEach { item ->
            when (item) {
                is GoalListItem.GoalHeader -> {
                    if (currentGoal != null) {
                        list.add(currentGoal!! to currentTasks)
                    }
                    currentGoal = item
                    currentTasks = mutableListOf()
                }
                is GoalListItem.TaskItem -> {
                    currentTasks.add(item.task)
                }
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
        items(
            items = groupedItems,
            key = { (header, _) -> header.goal.id }
        ) { (header, tasks) ->
            GoalCard(
                goal = header.goal,
                tasks = tasks,
                isExpanded = header.isExpanded,
                onToggle = { onGoalClick(header.goal.id) },
                onTaskClick = onTaskClick,
                onAddSubTask = { onAddTask(header.goal.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Preview(showBackground = true, name = "Main List Preview")
@Composable
fun GoalsListPreview() {
    GoalionTheme {
        GoalsList(
            items = MockData.previewItems,
            onGoalClick = {},
            onTaskClick = {},
            onAddTask = {}
        )
    }
}