package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.domain.model.GoalWithTasks
import com.skrpld.goalion.data.sources.local.GoalWithTasks as LocalGoalWithTasks

/**
 * Converts a LocalGoalWithTasks object to a GoalWithTasks domain object.
 *
 * @return A GoalWithTasks domain object with the same properties as the local object
 */
fun LocalGoalWithTasks.toDomain(): GoalWithTasks = GoalWithTasks(
    goal = goal.toDomain(),
    tasks = tasks.map { it.toDomain() }
)

/**
 * Converts a GoalWithTasks domain object to a LocalGoalWithTasks object.
 *
 * @param isSynced Flag indicating whether the entities are synchronized with the remote server
 * @param isDeleted Flag indicating whether the entities are marked for deletion
 * @return A LocalGoalWithTasks object with the same properties as the domain object
 */
fun GoalWithTasks.toEntity(
    isSynced: Boolean = false,
    isDeleted: Boolean = false
): LocalGoalWithTasks = LocalGoalWithTasks(
    goal = goal.toEntity(isSynced, isDeleted),
    tasks = tasks.map { it.toEntity(isSynced, isDeleted) }
)

/**
 * Converts a list of LocalGoalWithTasks objects to a list of GoalWithTasks domain objects.
 *
 * @return A list of GoalWithTasks domain objects
 */
fun List<LocalGoalWithTasks>.toDomain(): List<GoalWithTasks> = map { it.toDomain() }

/**
 * Converts a list of GoalWithTasks domain objects to a list of LocalGoalWithTasks objects.
 *
 * @param isSynced Flag indicating whether the entities are synchronized with the remote server
 * @param isDeleted Flag indicating whether the entities are marked for deletion
 * @return A list of LocalGoalWithTasks objects
 */
fun List<GoalWithTasks>.toEntity(
    isSynced: Boolean = false,
    isDeleted: Boolean = false
): List<LocalGoalWithTasks> = map { it.toEntity(isSynced, isDeleted) }