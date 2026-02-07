package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.entities.Task

// --- Entity-Domain ---

/**
 * Converts a TaskEntity to a Task domain object.
 *
 * @return A Task domain object with the same properties as the entity
 */
fun TaskEntity.toDomain(): Task = Task(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    startDate = startDate,
    targetDate = targetDate,
    updatedAt = updatedAt
)

/**
 * Converts a Task domain object to a TaskEntity.
 *
 * @param isSynced Flag indicating whether the entity is synchronized with the remote server
 * @param isDeleted Flag indicating whether the entity is marked for deletion
 * @return A TaskEntity with the same properties as the domain object
 */
fun Task.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): TaskEntity = TaskEntity(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    startDate = startDate,
    targetDate = targetDate,
    updatedAt = updatedAt,
    isSynced = isSynced,
    isDeleted = isDeleted
)

/**
 * Converts a list of TaskEntity objects to a list of Task domain objects.
 *
 * @return A list of Task domain objects
 */
fun List<TaskEntity>.toDomain(): List<Task> = map { it.toDomain() }

/**
 * Converts a list of Task domain objects to a list of TaskEntity objects.
 *
 * @param isSynced Flag indicating whether the entities are synchronized with the remote server
 * @param isDeleted Flag indicating whether the entities are marked for deletion
 * @return A list of TaskEntity objects
 */
fun List<Task>.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): List<TaskEntity> =
    map { it.toEntity(isSynced, isDeleted) }

// --- Entity-Network ---

/**
 * Converts a TaskEntity to a NetworkTask for remote API communication.
 *
 * @return A NetworkTask with the same properties as the entity
 */
fun TaskEntity.toNetwork(): NetworkTask = NetworkTask(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    startDate = startDate,
    targetDate = targetDate,
    updatedAt = null,
    isDeleted = isDeleted
)

/**
 * Converts a NetworkTask to a TaskEntity.
 *
 * @return A TaskEntity with the same properties as the network object
 */
fun NetworkTask.toEntity(): TaskEntity = TaskEntity(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    startDate = startDate,
    targetDate = targetDate,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis(),
    isSynced = true,
    isDeleted = isDeleted
)

/**
 * Converts a list of TaskEntity objects to a list of NetworkTask objects.
 *
 * @return A list of NetworkTask objects
 */
fun List<TaskEntity>.toNetwork(): List<NetworkTask> = map { it.toNetwork() }

/**
 * Converts a list of NetworkTask objects to a list of TaskEntity objects.
 *
 * @return A list of TaskEntity objects
 */
fun List<NetworkTask>.toEntity(): List<TaskEntity> = map { it.toEntity() }

// --- Network-Domain ---

/**
 * Converts a NetworkTask to a Task domain object.
 *
 * @return A Task domain object with the same properties as the network object
 */
fun NetworkTask.toDomain(): Task = Task(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    startDate = startDate,
    targetDate = targetDate,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis()
)

/**
 * Converts a Task domain object to a NetworkTask for remote API communication.
 *
 * @param isDeleted Flag indicating whether the network object represents a deleted task
 * @return A NetworkTask with the same properties as the domain object
 */
fun Task.toNetwork(isDeleted: Boolean = false): NetworkTask = NetworkTask(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    startDate = startDate,
    targetDate = targetDate,
    updatedAt = null,
    isDeleted = isDeleted
)

/**
 * Converts a list of NetworkTask objects to a list of Task domain objects.
 *
 * @return A list of Task domain objects
 */
fun List<NetworkTask>.asDomain(): List<Task> = map { it.toDomain() }

/**
 * Converts a list of Task domain objects to a list of NetworkTask objects.
 *
 * @param isDeleted Flag indicating whether the network objects represent deleted tasks
 * @return A list of NetworkTask objects
 */
fun List<Task>.asNetwork(isDeleted: Boolean = false): List<NetworkTask> = map { it.toNetwork(isDeleted) }