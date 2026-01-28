package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.entities.Profile

// --- Entity-Domain ---

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

fun List<ProfileEntity>.toDomain(): List<Profile> = map { it.toDomain() }
fun List<Profile>.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): List<ProfileEntity> =
    map { it.toEntity(isSynced, isDeleted) }

// --- Entity-Network ---

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

fun List<ProfileEntity>.toNetwork(): List<NetworkProfile> = map { it.toNetwork() }
fun List<NetworkProfile>.toEntity(): List<ProfileEntity> = map { it.toEntity() }

// --- Network-Domain ---

fun NetworkProfile.toDomain(): Profile = Profile(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis()
)

fun Profile.toNetwork(isDeleted: Boolean = false): NetworkProfile = NetworkProfile(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = null,
    isDeleted = isDeleted
)

fun List<NetworkProfile>.asDomain(): List<Profile> = map { it.toDomain() }
fun List<Profile>.asNetwork(isDeleted: Boolean = false): List<NetworkProfile> = map { it.toNetwork(isDeleted) }