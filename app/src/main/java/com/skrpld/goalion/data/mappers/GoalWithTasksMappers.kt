package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.domain.GoalWithTasks
import com.skrpld.goalion.data.local.GoalWithTasks as LocalGoalWithTasks

fun LocalGoalWithTasks.toDomain(): GoalWithTasks = GoalWithTasks(
    goal = goal.toDomain(),
    tasks = tasks.map { it.toDomain() }
)

fun GoalWithTasks.toEntity(
    isSynced: Boolean = false,
    isDeleted: Boolean = false
): LocalGoalWithTasks = LocalGoalWithTasks(
    goal = goal.toEntity(isSynced, isDeleted),
    tasks = tasks.map { it.toEntity(isSynced, isDeleted) }
)

fun List<LocalGoalWithTasks>.toDomain(): List<GoalWithTasks> = map { it.toDomain() }

fun List<GoalWithTasks>.toEntity(
    isSynced: Boolean = false,
    isDeleted: Boolean = false
): List<LocalGoalWithTasks> = map { it.toEntity(isSynced, isDeleted) }