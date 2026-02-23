package com.skrpld.goalion.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skrpld.goalion.data.mappers.*
import com.skrpld.goalion.data.sources.local.GoalDao
import com.skrpld.goalion.data.sources.local.ProfileDao
import com.skrpld.goalion.data.sources.local.TaskDao
import com.skrpld.goalion.data.sources.remote.GoalRemoteDataSource
import com.skrpld.goalion.data.sources.remote.ProfileRemoteDataSource
import com.skrpld.goalion.data.sources.remote.TaskRemoteDataSource
import kotlinx.coroutines.coroutineScope
import java.util.Date

/**
 * Synchronization worker for data between local and remote.
 * Performs bidirectional sync of profiles, goals, and tasks.
 * Implements a push-pull strategy to sync unsynchronized data.
 */
private const val TAG = "GoalionLog_SyncWorker"

class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val profileDao: ProfileDao,
    private val goalDao: GoalDao,
    private val taskDao: TaskDao,
    private val profileRemote: ProfileRemoteDataSource,
    private val goalRemote: GoalRemoteDataSource,
    private val taskRemote: TaskRemoteDataSource
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val userId = inputData.getString("USER_ID") ?: run {
            Log.e(TAG, "SyncWorker failed: USER_ID is null")
            return@coroutineScope Result.failure()
        }

        Log.i(TAG, "--- SYNC STARTED for User: $userId ---")

        try {
            Log.d(TAG, "[PUSH] Starting push process...")
            pushProfiles()
            pushGoals()
            pushTasks()

            Log.d(TAG, "[PULL] Starting pull process...")
            pullProfiles(userId)

            val allProfiles = profileDao.getProfilesByUser(userId)
            val profileIds = allProfiles.map { it.id }

            if (profileIds.isNotEmpty()) {
                pullGoals(profileIds)
                val goalIds = goalDao.getGoalIdsByProfileIds(profileIds)
                if (goalIds.isNotEmpty()) pullTasks(goalIds)
            } else {
                Log.d(TAG, "[PULL] No local profiles found, skipping goals and tasks pull.")
            }

            Log.i(TAG, "--- SYNC COMPLETED SUCCESSFULLY ---")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "--- SYNC FAILED --- Error: ${e.message}", e)
            if (runAttemptCount < 3) {
                Log.w(TAG, "Retrying sync... Attempt: $runAttemptCount")
                Result.retry()
            } else {
                Log.e(TAG, "Max retries reached. Sync completely failed.")
                Result.failure()
            }
        }
    }

    /** Push unsynchronized profiles to remote */
    private suspend fun pushProfiles() {
        val unsynced = profileDao.getUnsynced()
        Log.d(TAG, "[PUSH] Profiles to upload: ${unsynced.size}")
        unsynced.forEach { entity ->
            try {
                if (entity.isDeleted) {
                    Log.d(TAG, "[PUSH] Deleting Profile on remote: ${entity.id}")
                    profileRemote.deleteProfile(entity.id)
                    profileDao.hardDelete(entity.id)
                } else {
                    Log.d(TAG, "[PUSH] Upserting Profile on remote: ${entity.id} (Title: ${entity.title})")
                    profileRemote.upsertProfile(entity.toNetwork())
                    profileDao.markSynced(entity.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[PUSH ERROR] Failed to push Profile ${entity.id}", e)
                throw e
            }
        }
    }

    /** Push unsynchronized goals to remote */
    private suspend fun pushGoals() {
        val unsynced = goalDao.getUnsynced()
        Log.d(TAG, "[PUSH] Goals to upload: ${unsynced.size}")
        unsynced.forEach { entity ->
            try {
                if (entity.isDeleted) {
                    Log.d(TAG, "[PUSH] Deleting Goal on remote: ${entity.id}")
                    goalRemote.deleteGoal(entity.id)
                    goalDao.hardDelete(entity.id)
                } else {
                    Log.d(TAG, "[PUSH] Upserting Goal on remote: ${entity.id} (Title: ${entity.title})")
                    goalRemote.upsertGoal(entity.toNetwork())
                    goalDao.markSynced(entity.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[PUSH ERROR] Failed to push Goal ${entity.id}", e)
                throw e
            }
        }
    }

    /** Push unsynchronized tasks to remote */
    private suspend fun pushTasks() {
        val unsynced = taskDao.getUnsynced()
        Log.d(TAG, "[PUSH] Tasks to upload: ${unsynced.size}")
        unsynced.forEach { entity ->
            try {
                if (entity.isDeleted) {
                    Log.d(TAG, "[PUSH] Deleting Task on remote: ${entity.id}")
                    taskRemote.deleteTask(entity.id)
                    taskDao.hardDelete(entity.id)
                } else {
                    Log.d(TAG, "[PUSH] Upserting Task on remote: ${entity.id} (Title: ${entity.title})")
                    taskRemote.upsertTask(entity.toNetwork())
                    taskDao.markSynced(entity.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[PUSH ERROR] Failed to push Task ${entity.id}", e)
                throw e
            }
        }
    }

    /**
     * === Pull ===
     */

    /** Pull updated profiles from remote */
    private suspend fun pullProfiles(userId: String) {
        val lastUpdate = profileDao.getLastUpdateTime(userId) ?: 0L
        Log.d(TAG, "[PULL] Fetching Profiles updated after timestamp: $lastUpdate")
        val remoteProfiles = profileRemote.getProfilesUpdatedAfter(userId, lastUpdate)

        if (remoteProfiles.isNotEmpty()) {
            Log.d(TAG, "[PULL] Downloaded ${remoteProfiles.size} Profiles from remote. Saving to local DB...")
            profileDao.upsertAll(remoteProfiles.map { it.toEntity() })
        } else {
            Log.d(TAG, "[PULL] No new Profiles found on remote.")
        }
    }

    /** Pull updated goals from remote */
    private suspend fun pullGoals(profileIds: List<String>) {
        val lastUpdate = goalDao.getLastUpdateTime(profileIds) ?: 0L
        val date = Date(lastUpdate)
        Log.d(TAG, "[PULL] Fetching Goals updated after date: $date for ${profileIds.size} profiles")

        var totalDownloaded = 0
        profileIds.forEach { pid ->
            val remoteGoals = goalRemote.getGoalsUpdatedAfter(pid, date)
            if (remoteGoals.isNotEmpty()) {
                totalDownloaded += remoteGoals.size
                goalDao.upsertAll(remoteGoals.map { it.toEntity() })
            }
        }
        Log.d(TAG, "[PULL] Downloaded a total of $totalDownloaded Goals from remote.")
    }

    /** Pull updated tasks from remote */
    private suspend fun pullTasks(goalIds: List<String>) {
        val lastUpdate = taskDao.getLastUpdateTime(goalIds) ?: 0L
        val date = Date(lastUpdate)
        Log.d(TAG, "[PULL] Fetching Tasks updated after date: $date for ${goalIds.size} goals")

        var totalDownloaded = 0
        goalIds.forEach { gid ->
            val remoteTasks = taskRemote.getTasksUpdatedAfter(gid, date)
            if (remoteTasks.isNotEmpty()) {
                totalDownloaded += remoteTasks.size
                taskDao.upsertAll(remoteTasks.map { it.toEntity() })
            }
        }
        Log.d(TAG, "[PULL] Downloaded a total of $totalDownloaded Tasks from remote.")
    }
}