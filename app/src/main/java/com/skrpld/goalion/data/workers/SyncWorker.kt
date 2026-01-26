package com.skrpld.goalion.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skrpld.goalion.data.local.GoalDao
import com.skrpld.goalion.data.local.ProfileDao
import com.skrpld.goalion.data.local.TaskDao
import com.skrpld.goalion.data.mappers.toNetwork
import com.skrpld.goalion.data.remote.GoalRemoteDataSource
import com.skrpld.goalion.data.remote.ProfileRemoteDataSource
import com.skrpld.goalion.data.remote.TaskRemoteDataSource
import kotlinx.coroutines.coroutineScope

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
        try {
            val userId = inputData.getString("USER_ID") ?: return@coroutineScope Result.failure()

            val unsyncedProfiles = profileDao.getUnsynced()
            unsyncedProfiles.forEach {
                profileRemote.upsertProfile(it.toNetwork())
                profileDao.markSynced(it.id)
            }

            val unsyncedGoals = goalDao.getUnsynced()
            unsyncedGoals.forEach {
                goalRemote.upsertGoal(it.toNetwork())
                goalDao.markSynced(it.id)
            }

            val unsyncedTasks = taskDao.getUnsynced()
            unsyncedTasks.forEach {
                taskRemote.upsertTask(it.toNetwork())
                taskDao.markSynced(it.id)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}