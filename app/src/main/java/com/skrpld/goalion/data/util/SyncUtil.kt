package com.skrpld.goalion.data.util

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.skrpld.goalion.data.sources.local.GoalDao
import com.skrpld.goalion.data.sources.local.ProfileDao
import com.skrpld.goalion.data.sources.local.TaskDao
import com.skrpld.goalion.data.workers.SyncWorker

/**
 * Utility class for handling synchronization operations across repositories.
 * Provides common methods for scheduling sync operations based on entity relationships.
 */
private const val TAG = "GoalionLog_SyncUtil"

class SyncUtil(
    private val goalDao: GoalDao,
    private val profileDao: ProfileDao,
    private val workManager: WorkManager
) {
    
    /**
     * Triggers a sync operation for the profile associated with the specified goal.
     *
     * @param goalId The unique identifier of the goal
     */
    suspend fun triggerSyncByGoalId(goalId: String) {
        Log.d(TAG, "Sync triggered by GoalId: $goalId")
        val goal = goalDao.getGoal(goalId) ?: run {
            Log.w(TAG, "triggerSyncByGoalId: Goal $goalId not found in local DB")
            return
        }
        scheduleSyncByProfileId(goal.profileId)
    }
    
    /**
     * Triggers a sync operation for the profile associated with the specified task.
     *
     * @param taskId The unique identifier of the task
     */
    suspend fun triggerSyncByTaskId(taskId: String, taskDao: TaskDao) {
        Log.d(TAG, "Sync triggered by TaskId: $taskId")
        val task = taskDao.getTask(taskId) ?: run {
            Log.w(TAG, "triggerSyncByTaskId: Task $taskId not found in local DB")
            return
        }
        scheduleSync(task.goalId)
    }
    
    /**
     * Schedules a sync operation for the user associated with the specified goal.
     *
     * @param goalId The unique identifier of the goal
     */
    suspend fun scheduleSync(goalId: String) {
        val goal = goalDao.getGoal(goalId) ?: return
        val profile = profileDao.getProfile(goal.profileId) ?: return
        Log.d(TAG, "Scheduling sync via goal $goalId -> profile ${profile.id} -> user ${profile.userId}")
        enqueueWorker(profile.userId)
    }
    
    /**
     * Schedules a sync operation for the user associated with the specified profile.
     *
     * @param profileId The unique identifier of the profile
     */
    suspend fun scheduleSyncByProfileId(profileId: String) {
        val profile = profileDao.getProfile(profileId) ?: run {
            Log.w(TAG, "scheduleSyncByProfileId: Profile $profileId not found in local DB")
            return
        }
        Log.d(TAG, "Scheduling sync by ProfileId: $profileId -> user ${profile.userId}")
        enqueueWorker(profile.userId)
    }
    
    /**
     * Schedules a sync operation for the specified user.
     *
     * @param userId The unique identifier of the user
     */
    fun scheduleSyncByUserId(userId: String) {
        Log.d(TAG, "Scheduling direct sync for UserId: $userId")
        enqueueWorker(userId)
    }
    
    /**
     * Enqueues a sync worker for the specified user.
     *
     * @param userId The unique identifier of the user to sync
     */
    private fun enqueueWorker(userId: String) {
        Log.d(TAG, "Configuring WorkRequest for User: $userId")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("USER_ID" to userId))
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        Log.d(TAG, "Enqueuing Unique Work: sync_user_$userId")
        workManager.enqueueUniqueWork(
            "sync_user_$userId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}