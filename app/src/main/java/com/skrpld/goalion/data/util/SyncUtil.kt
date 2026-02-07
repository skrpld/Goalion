package com.skrpld.goalion.data.util

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.skrpld.goalion.data.local.GoalDao
import com.skrpld.goalion.data.local.ProfileDao
import com.skrpld.goalion.data.local.TaskDao
import com.skrpld.goalion.data.workers.SyncWorker

/**
 * Utility class for handling synchronization operations across repositories.
 * Provides common methods for scheduling sync operations based on entity relationships.
 */
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
        val goal = goalDao.getGoal(goalId) ?: return
        scheduleSyncByProfileId(goal.profileId)
    }
    
    /**
     * Triggers a sync operation for the profile associated with the specified task.
     *
     * @param taskId The unique identifier of the task
     */
    suspend fun triggerSyncByTaskId(taskId: String, taskDao: TaskDao) {
        val task = taskDao.getTask(taskId) ?: return
        scheduleSyncByProfileId(task.goalId)
    }
    
    /**
     * Schedules a sync operation for the user associated with the specified goal.
     *
     * @param goalId The unique identifier of the goal
     */
    suspend fun scheduleSync(goalId: String) {
        val goal = goalDao.getGoal(goalId) ?: return
        val profile = profileDao.getProfile(goal.profileId) ?: return
        
        enqueueWorker(profile.userId)
    }
    
    /**
     * Schedules a sync operation for the user associated with the specified profile.
     *
     * @param profileId The unique identifier of the profile
     */
    suspend fun scheduleSyncByProfileId(profileId: String) {
        val profile = profileDao.getProfile(profileId) ?: return
        enqueueWorker(profile.userId)
    }
    
    /**
     * Schedules a sync operation for the specified user.
     *
     * @param userId The unique identifier of the user
     */
    fun scheduleSyncByUserId(userId: String) {
        enqueueWorker(userId)
    }
    
    /**
     * Enqueues a sync worker for the specified user.
     *
     * @param userId The unique identifier of the user to sync
     */
    private fun enqueueWorker(userId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("USER_ID" to userId))
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniqueWork(
            "sync_user_$userId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}