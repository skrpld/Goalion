package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.*

fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    profileId = profileId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = updatedAt
)

fun Goal.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): GoalEntity = GoalEntity(
    id = id,
    profileId = profileId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = updatedAt,
    isSynced = isSynced,
    isDeleted = isDeleted
)

fun GoalEntity.toNetwork(): NetworkGoal = NetworkGoal(
    id = id,
    profileId = profileId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = null,
    isDeleted = isDeleted
)

fun NetworkGoal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    profileId = profileId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis(),
    isSynced = true,
    isDeleted = isDeleted
)