package com.skrpld.goalion.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skrpld.goalion.data.local.*
import com.skrpld.goalion.data.mappers.*
import com.skrpld.goalion.data.remote.*
import kotlinx.coroutines.coroutineScope
import java.util.Date

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
        val userId = inputData.getString("USER_ID") ?: return@coroutineScope Result.failure()

        try {
            pushProfiles()
            pushGoals()
            pushTasks()

            pullProfiles(userId)

            val allProfiles = profileDao.getProfilesByUser(userId)
            val profileIds = allProfiles.map { it.id }

            if (profileIds.isNotEmpty()) {
                pullGoals(profileIds)

                val goalIds = goalDao.getGoalIdsByProfileIds(profileIds)

                if (goalIds.isNotEmpty()) pullTasks(goalIds)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    /**
     * === Push ===
     */
    private suspend fun pushProfiles() {
        val unsynced = profileDao.getUnsynced()
        unsynced.forEach { entity ->
            try {
                if (entity.isDeleted) {
                    profileRemote.deleteProfile(entity.id)
                    profileDao.hardDelete(entity.id)
                } else {
                    profileRemote.upsertProfile(entity.toNetwork())
                    profileDao.markSynced(entity.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    private suspend fun pushGoals() {
        val unsynced = goalDao.getUnsynced()
        unsynced.forEach { entity ->
            try {
                if (entity.isDeleted) {
                    goalRemote.deleteGoal(entity.id)
                    goalDao.hardDelete(entity.id)
                } else {
                    goalRemote.upsertGoal(entity.toNetwork())
                    goalDao.markSynced(entity.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    private suspend fun pushTasks() {
        val unsynced = taskDao.getUnsynced()
        unsynced.forEach { entity ->
            try {
                if (entity.isDeleted) {
                    taskRemote.deleteTask(entity.id)
                    taskDao.hardDelete(entity.id)
                } else {
                    taskRemote.upsertTask(entity.toNetwork())
                    taskDao.markSynced(entity.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    /**
     * === Pull ===
     */
    private suspend fun pullProfiles(userId: String) {
        val lastUpdate = profileDao.getLastUpdateTime(userId) ?: 0L
        val remoteProfiles = profileRemote.getProfilesUpdatedAfter(userId, lastUpdate)

        if (remoteProfiles.isNotEmpty()) {
            profileDao.upsertAll(remoteProfiles.map { it.toEntity() })
        }
    }

    private suspend fun pullGoals(profileIds: List<String>) {
        val lastUpdate = goalDao.getLastUpdateTime(profileIds) ?: 0L
        val date = Date(lastUpdate)

        profileIds.forEach { pid ->
            val remoteGoals = goalRemote.getGoalsUpdatedAfter(pid, date)
            if (remoteGoals.isNotEmpty()) {
                goalDao.upsertAll(remoteGoals.map { it.toEntity() })
            }
        }
    }

    private suspend fun pullTasks(goalIds: List<String>) {
        val lastUpdate = taskDao.getLastUpdateTime(goalIds) ?: 0L
        val date = Date(lastUpdate)

        goalIds.forEach { gid ->
            val remoteTasks = taskRemote.getTasksUpdatedAfter(gid, date)
            if (remoteTasks.isNotEmpty()) {
                taskDao.upsertAll(remoteTasks.map { it.toEntity() })
            }
        }
    }
}