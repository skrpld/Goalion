package com.skrpld.goalion.domain.usecases

import android.util.Log
import com.skrpld.goalion.domain.model.Profile
import com.skrpld.goalion.domain.repositories.ProfileRepository

private const val TAG = "GoalionLog_ProfileUC"

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
        Log.d(TAG, "Getting profiles for user: $userId")
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
        Log.d(TAG, "Creating new Profile for user: $userId. Title: $title")
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val profile = Profile(
            userId = userId,
            title = title,
            description = description
        )

        Log.d(TAG, "Passing new profile to repository for upsert and sync")
        profileRepository.upsert(profile)
    }
}

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(id: String, userId: String, title: String, description: String) {
        Log.d(TAG, "Updating Profile ID: $id. Title: $title")
        if (id.isBlank()) throw IllegalArgumentException("Profile ID cannot be empty for update")
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val profile = Profile(
            id = id,
            userId = userId,
            title = title,
            description = description
        )

        Log.d(TAG, "Passing updated profile to repository for upsert and sync")
        profileRepository.upsert(profile)
    }
}

class DeleteProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profileId: String) {
        Log.w(TAG, "Deleting Profile ID: $profileId")
        profileRepository.delete(profileId)
    }
}

class SyncProfilesUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String) {
        Log.d(TAG, "[SYNC] Triggering manual sync for user: $userId")
        profileRepository.sync(userId)
    }
}