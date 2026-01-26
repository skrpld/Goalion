package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.*

fun ProfileEntity.toDomain(): Profile = Profile(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = updatedAt
)

fun Profile.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): ProfileEntity = ProfileEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = updatedAt,
    isSynced = isSynced,
    isDeleted = isDeleted
)

fun ProfileEntity.toNetwork(): NetworkProfile = NetworkProfile(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = null,
    isDeleted = isDeleted
)

fun NetworkProfile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis(),
    isSynced = true,
    isDeleted = isDeleted
)