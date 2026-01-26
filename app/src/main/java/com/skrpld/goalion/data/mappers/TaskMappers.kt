package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.*

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
