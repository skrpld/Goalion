package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.entities.User

// --- Entity-Domain ---

/** Convert UserEntity to User domain object */
fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    updatedAt = updatedAt
)

/** Convert User domain object to UserEntity */
fun User.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    updatedAt = updatedAt,
    isSynced = isSynced,
    isDeleted = isDeleted
)

/** Convert list of UserEntity to list of User domain objects */
fun List<UserEntity>.toDomain(): List<User> = map { it.toDomain() }

/** Convert list of User domain objects to list of UserEntity */
fun List<User>.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): List<UserEntity> =
    map { it.toEntity(isSynced, isDeleted) }

// --- Entity-Network ---

/** Convert UserEntity to NetworkUser */
fun UserEntity.toNetwork(): NetworkUser = NetworkUser(
    id = id,
    name = name,
    email = email,
    updatedAt = null,
    isDeleted = isDeleted
)

/** Convert NetworkUser to UserEntity */
fun NetworkUser.toEntity(currentPassword: String = ""): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis(),
    isSynced = true,
    isDeleted = isDeleted
)

/** Convert list of UserEntity to list of NetworkUser */
fun List<UserEntity>.toNetwork(): List<NetworkUser> = map { it.toNetwork() }

/** Convert list of NetworkUser to list of UserEntity */
fun List<NetworkUser>.toEntity(currentPassword: String = ""): List<UserEntity> =
    map { it.toEntity(currentPassword) }

// --- Network-Domain ---

/** Convert NetworkUser to User domain object */
fun NetworkUser.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis()
)

/** Convert User domain object to NetworkUser */
fun User.toNetwork(isDeleted: Boolean = false): NetworkUser = NetworkUser(
    id = id,
    name = name,
    email = email,
    updatedAt = null,
    isDeleted = isDeleted
)

/** Convert list of NetworkUser to list of User domain objects */
fun List<NetworkUser>.asDomain(): List<User> = map { it.toDomain() }

/** Convert list of User domain objects to list of NetworkUser */
fun List<User>.asNetwork(isDeleted: Boolean = false): List<NetworkUser> = map { it.toNetwork(isDeleted) }