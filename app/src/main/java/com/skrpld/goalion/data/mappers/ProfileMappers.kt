package com.skrpld.goalion.data.mappers

import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.remote.*
import com.skrpld.goalion.domain.entities.Profile

// --- Entity-Domain ---

/**
 * Converts a ProfileEntity to a Profile domain object.
 *
 * @return A Profile domain object with the same properties as the entity
 */
fun ProfileEntity.toDomain(): Profile = Profile(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = updatedAt
)

/**
 * Converts a Profile domain object to a ProfileEntity.
 *
 * @param isSynced Flag indicating whether the entity is synchronized with the remote server
 * @param isDeleted Flag indicating whether the entity is marked for deletion
 * @return A ProfileEntity with the same properties as the domain object
 */
fun Profile.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): ProfileEntity = ProfileEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = updatedAt,
    isSynced = isSynced,
    isDeleted = isDeleted
)

/**
 * Converts a list of ProfileEntity objects to a list of Profile domain objects.
 *
 * @return A list of Profile domain objects
 */
fun List<ProfileEntity>.toDomain(): List<Profile> = map { it.toDomain() }

/**
 * Converts a list of Profile domain objects to a list of ProfileEntity objects.
 *
 * @param isSynced Flag indicating whether the entities are synchronized with the remote server
 * @param isDeleted Flag indicating whether the entities are marked for deletion
 * @return A list of ProfileEntity objects
 */
fun List<Profile>.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false): List<ProfileEntity> =
    map { it.toEntity(isSynced, isDeleted) }

// --- Entity-Network ---

/**
 * Converts a ProfileEntity to a NetworkProfile for remote API communication.
 *
 * @return A NetworkProfile with the same properties as the entity
 */
fun ProfileEntity.toNetwork(): NetworkProfile = NetworkProfile(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = null,
    isDeleted = isDeleted
)

/**
 * Converts a NetworkProfile to a ProfileEntity.
 *
 * @return A ProfileEntity with the same properties as the network object
 */
fun NetworkProfile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis(),
    isSynced = true,
    isDeleted = isDeleted
)

/**
 * Converts a list of ProfileEntity objects to a list of NetworkProfile objects.
 *
 * @return A list of NetworkProfile objects
 */
fun List<ProfileEntity>.toNetwork(): List<NetworkProfile> = map { it.toNetwork() }

/**
 * Converts a list of NetworkProfile objects to a list of ProfileEntity objects.
 *
 * @return A list of ProfileEntity objects
 */
fun List<NetworkProfile>.toEntity(): List<ProfileEntity> = map { it.toEntity() }

// --- Network-Domain ---

/**
 * Converts a NetworkProfile to a Profile domain object.
 *
 * @return A Profile domain object with the same properties as the network object
 */
fun NetworkProfile.toDomain(): Profile = Profile(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = updatedAt?.time ?: System.currentTimeMillis()
)

/**
 * Converts a Profile domain object to a NetworkProfile for remote API communication.
 *
 * @param isDeleted Flag indicating whether the network object represents a deleted profile
 * @return A NetworkProfile with the same properties as the domain object
 */
fun Profile.toNetwork(isDeleted: Boolean = false): NetworkProfile = NetworkProfile(
    id = id,
    userId = userId,
    title = title,
    description = description,
    updatedAt = null,
    isDeleted = isDeleted
)

/**
 * Converts a list of NetworkProfile objects to a list of Profile domain objects.
 *
 * @return A list of Profile domain objects
 */
fun List<NetworkProfile>.asDomain(): List<Profile> = map { it.toDomain() }

/**
 * Converts a list of Profile domain objects to a list of NetworkProfile objects.
 *
 * @param isDeleted Flag indicating whether the network objects represent deleted profiles
 * @return A list of NetworkProfile objects
 */
fun List<Profile>.asNetwork(isDeleted: Boolean = false): List<NetworkProfile> = map { it.toNetwork(isDeleted) }