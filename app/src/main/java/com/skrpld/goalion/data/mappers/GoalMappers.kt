package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.entities.Goal

// --- Entity-Domain ---

/**
 * Converts a GoalEntity to a Goal domain object.
 *
 * @return A Goal domain object with the same properties as the entity
 */
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

/**
 * Converts a Goal domain object to a GoalEntity.
 *
 * @param isSynced Flag indicating whether the entity is synchronized with the remote server
 * @param isDeleted Flag indicating whether the entity is marked for deletion
 * @return A GoalEntity with the same properties as the domain object
 */
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

/**
 * Converts a list of GoalEntity objects to a list of Goal domain objects.
 *
 * @return A list of Goal domain objects
 */
fun List<GoalEntity>.toDomain(): List<Goal> = map { it.toDomain() }

/**
 * Converts a list of Goal domain objects to a list of GoalEntity objects.
 *
 * @param isSynced Flag indicating whether the entities are synchronized with the remote server
 * @param isDeleted Flag indicating whether the entities are marked for deletion
 * @return A list of GoalEntity objects
 */
fun List<Goal>.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): List<GoalEntity> =
    map { it.toEntity(isSynced, isDeleted) }

// --- Entity-Network ---

/**
 * Converts a GoalEntity to a NetworkGoal for remote API communication.
 *
 * @return A NetworkGoal with the same properties as the entity
 */
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

/**
 * Converts a NetworkGoal to a GoalEntity.
 *
 * @return A GoalEntity with the same properties as the network object
 */
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

/**
 * Converts a list of GoalEntity objects to a list of NetworkGoal objects.
 *
 * @return A list of NetworkGoal objects
 */
fun List<GoalEntity>.toNetwork(): List<NetworkGoal> = map { it.toNetwork() }

/**
 * Converts a list of NetworkGoal objects to a list of GoalEntity objects.
 *
 * @return A list of GoalEntity objects
 */
fun List<NetworkGoal>.toEntity(): List<GoalEntity> = map { it.toEntity() }

// --- Network-Domain ---

/**
 * Converts a NetworkGoal to a Goal domain object.
 *
 * @return A Goal domain object with the same properties as the network object
 */
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

/**
 * Converts a Goal domain object to a NetworkGoal for remote API communication.
 *
 * @param isDeleted Flag indicating whether the network object represents a deleted goal
 * @return A NetworkGoal with the same properties as the domain object
 */
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

/**
 * Converts a list of NetworkGoal objects to a list of Goal domain objects.
 *
 * @return A list of Goal domain objects
 */
fun List<NetworkGoal>.asDomain(): List<Goal> = map { it.toDomain() }

/**
 * Converts a list of Goal domain objects to a list of NetworkGoal objects.
 *
 * @param isDeleted Flag indicating whether the network objects represent deleted goals
 * @return A list of NetworkGoal objects
 */
fun List<Goal>.asNetwork(isDeleted: Boolean = false): List<NetworkGoal> = map { it.toNetwork(isDeleted) }