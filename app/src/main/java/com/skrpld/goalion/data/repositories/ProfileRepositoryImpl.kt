package com.skrpld.goalion.data.repositories

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.skrpld.goalion.data.local.ProfileDao
import com.skrpld.goalion.data.mappers.toDomain
import com.skrpld.goalion.data.mappers.toEntity
import com.skrpld.goalion.data.workers.SyncWorker
import com.skrpld.goalion.domain.Profile
import com.skrpld.goalion.domain.repositories.ProfileRepository

class ProfileRepositoryImpl(
    private val profileDao: ProfileDao,
    private val workManager: WorkManager
) : ProfileRepository {

    override suspend fun getProfilesByUser(userId: String): List<Profile> {
        return profileDao.getProfilesByUser(userId).map { it.toDomain() }
    }

    override suspend fun upsertProfile(profile: Profile) {
        profileDao.upsert(profile.toEntity())
        startSync(profile.userId)
    }

    override suspend fun deleteProfile(profileId: String) {
        profileDao.softDelete(profileId)
    }

    override suspend fun syncProfiles(userId: String) {
        startSync(userId)
    }

    private fun startSync(userId: String) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("USER_ID" to userId))
            .build()
        workManager.enqueue(request)
    }
}