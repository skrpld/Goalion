package com.skrpld.goalion.domain.usecases

import com.skrpld.goalion.domain.entities.Profile
import com.skrpld.goalion.domain.repositories.ProfileRepository

data class ProfileInteractors(
    val getProfiles: GetProfilesUseCases,
    val create: CreateProfileUseCase,
    val update: UpdateProfileUseCase,
    val delete: DeleteProfileUseCase,
    val sync: SyncProfilesUseCase
)

/**
 * Gets all profiles for a user.
 * Retrieves profiles from the profile repository.
 */
class GetProfilesUseCases(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String): List<Profile> {
        return profileRepository.getProfilesByUser(userId)
    }
}

/**
 * Creates a new profile.
 * Generates a new ID and saves the profile.
 */
class CreateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String, title: String, description: String) {
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val profile = Profile(
            userId = userId,
            title = title,
            description = description
        )

        profileRepository.upsert(profile)
    }
}

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(id: String, userId: String, title: String, description: String) {
        if (id.isBlank()) throw IllegalArgumentException("Profile ID cannot be empty for update")

        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val profile = Profile(
            id = id,
            userId = userId,
            title = title,
            description = description
        )

        profileRepository.upsert(profile)
    }
}

class DeleteProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profileId: String) {
        profileRepository.delete(profileId)
    }
}

class SyncProfilesUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String) {
        profileRepository.sync(userId)
    }
}