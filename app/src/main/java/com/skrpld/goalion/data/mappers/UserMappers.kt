package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.*

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    password = password,
    updatedAt = updatedAt
)

fun User.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    password = password,
    updatedAt = updatedAt,
    isSynced = isSynced,
    isDeleted = isDeleted
)

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
    password = currentPassword,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis(),
    isSynced = true,
    isDeleted = isDeleted
)
