package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.entities.Goal

// --- Entity-Domain ---

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

fun List<GoalEntity>.toDomain(): List<Goal> = map { it.toDomain() }
fun List<Goal>.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): List<GoalEntity> =
    map { it.toEntity(isSynced, isDeleted) }

// --- Entity-Network ---

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

fun List<GoalEntity>.toNetwork(): List<NetworkGoal> = map { it.toNetwork() }
fun List<NetworkGoal>.toEntity(): List<GoalEntity> = map { it.toEntity() }

// --- Network-Domain ---

fun NetworkGoal.toDomain(): Goal = Goal(
    id = id,
    profileId = profileId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    order = order,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis()
)

fun Goal.toNetwork(isDeleted: Boolean = false): NetworkGoal = NetworkGoal(
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

fun List<NetworkGoal>.asDomain(): List<Goal> = map { it.toDomain() }
fun List<Goal>.asNetwork(isDeleted: Boolean = false): List<NetworkGoal> = map { it.toNetwork(isDeleted) }