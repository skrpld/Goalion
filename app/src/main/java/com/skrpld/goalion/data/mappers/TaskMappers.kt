package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.entities.Task

// --- Entity-Domain ---

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = updatedAt
)

fun Task.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): TaskEntity = TaskEntity(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = updatedAt,
    isSynced = isSynced,
    isDeleted = isDeleted
)

fun List<TaskEntity>.toDomain(): List<Task> = map { it.toDomain() }
fun List<Task>.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): List<TaskEntity> =
    map { it.toEntity(isSynced, isDeleted) }

// --- Entity-Network ---

fun TaskEntity.toNetwork(): NetworkTask = NetworkTask(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = null,
    isDeleted = isDeleted
)

fun NetworkTask.toEntity(): TaskEntity = TaskEntity(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis(),
    isSynced = true,
    isDeleted = isDeleted
)

fun List<TaskEntity>.toNetwork(): List<NetworkTask> = map { it.toNetwork() }
fun List<NetworkTask>.toEntity(): List<TaskEntity> = map { it.toEntity() }

// --- Network-Domain ---

fun NetworkTask.toDomain(): Task = Task(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis()
)

fun Task.toNetwork(isDeleted: Boolean = false): NetworkTask = NetworkTask(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = null,
    isDeleted = isDeleted
)

fun List<NetworkTask>.asDomain(): List<Task> = map { it.toDomain() }
fun List<Task>.asNetwork(isDeleted: Boolean = false): List<NetworkTask> = map { it.toNetwork(isDeleted) }