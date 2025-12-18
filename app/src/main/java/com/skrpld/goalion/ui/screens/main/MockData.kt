package com.skrpld.goalion.ui.screens.main

import com.skrpld.goalion.data.models.Goal
import com.skrpld.goalion.data.models.Task

object MockData {
    val profileId = 1

    val goals = listOf(
        Goal(id = 1, title = "Изучить Jetpack Compose", profileId = profileId, orderIndex = 0, priority = 0),
        Goal(id = 2, title = "Сходить в спортзал", profileId = profileId, orderIndex = 1, priority = 1),
        Goal(id = 3, title = "Прочитать 10 книг", profileId = profileId, orderIndex = 2, priority = 2)
    )

    val tasks = listOf(
        Task(id = 1, goalId = 1, title = "Посмотреть курс по Side Effects", description = "", orderIndex = 0),
        Task(id = 2, goalId = 1, title = "Написать кастомный Layout", description = "", orderIndex = 1),
        Task(id = 3, goalId = 2, title = "Приседания 3х15", description = "", orderIndex = 0),
        Task(id = 4, goalId = 2, title = "Бег 5км", description = "", orderIndex = 1),
    )

    val previewItems = listOf(
        GoalListItem.GoalHeader(goals[0], isExpanded = true),
        GoalListItem.TaskItem(tasks[0]),
        GoalListItem.TaskItem(tasks[1]),
        GoalListItem.GoalHeader(goals[1], isExpanded = false),
        GoalListItem.GoalHeader(goals[2], isExpanded = true),
        GoalListItem.TaskItem(tasks[2])
    )
}