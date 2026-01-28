package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.entities.User

// --- Entity-Domain ---

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    updatedAt = updatedAt
)

fun User.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    updatedAt = updatedAt,
    isSynced = isSynced,
    isDeleted = isDeleted
)

fun List<UserEntity>.toDomain(): List<User> = map { it.toDomain() }
fun List<User>.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): List<UserEntity> =
    map { it.toEntity(isSynced, isDeleted) }

// --- Entity-Network ---

fun UserEntity.toNetwork(): NetworkUser = NetworkUser(
    id = id,
    name = name,
    email = email,
    updatedAt = null,
    isDeleted = isDeleted
)

fun NetworkUser.toEntity(currentPassword: String = ""): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis(),
    isSynced = true,
    isDeleted = isDeleted
)

fun List<UserEntity>.toNetwork(): List<NetworkUser> = map { it.toNetwork() }
fun List<NetworkUser>.toEntity(currentPassword: String = ""): List<UserEntity> =
    map { it.toEntity(currentPassword) }

// --- Network-Domain ---

fun NetworkUser.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis()
)

fun User.toNetwork(isDeleted: Boolean = false): NetworkUser = NetworkUser(
    id = id,
    name = name,
    email = email,
    updatedAt = null,
    isDeleted = isDeleted
)

fun List<NetworkUser>.asDomain(): List<User> = map { it.toDomain() }
fun List<User>.asNetwork(isDeleted: Boolean = false): List<NetworkUser> = map { it.toNetwork(isDeleted) }