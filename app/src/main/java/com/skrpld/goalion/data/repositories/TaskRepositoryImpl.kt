package com.skrpld.goalion.data.repositories

import androidx.work.*
import com.skrpld.goalion.data.local.GoalDao
import com.skrpld.goalion.data.local.ProfileDao
import com.skrpld.goalion.data.local.TaskDao
import com.skrpld.goalion.data.mappers.toEntity
import com.skrpld.goalion.data.workers.SyncWorker
import com.skrpld.goalion.domain.Task
import com.skrpld.goalion.domain.repositories.TaskRepository

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val goalDao: GoalDao,
    private val profileDao: ProfileDao,
    private val workManager: WorkManager
) : TaskRepository {

    override suspend fun upsertTask(task: Task) {
        taskDao.upsert(task.toEntity())
        scheduleSync(task.goalId)
    }

    override suspend fun deleteTask(taskId: String) {
        val task = taskDao.getTask(taskId)
        taskDao.softDelete(taskId)
        task?.let { scheduleSync(it.goalId) }
    }

    override suspend fun updateTaskStatus(taskId: String, status: Boolean) {
        taskDao.updateStatus(taskId, status)
        triggerSyncByTaskId(taskId)
    }

    override suspend fun updateTaskPriority(taskId: String, priority: Int) {
        taskDao.updatePriority(taskId, priority)
        triggerSyncByTaskId(taskId)
    }

    override suspend fun updateTaskOrder(taskId: String, order: Int) {
        taskDao.updateOrder(taskId, order)
        triggerSyncByTaskId(taskId)
    }

    override suspend fun syncTask(goalId: String) {
        scheduleSync(goalId)
    }

    /**
     * === Sync ===
     */
    private suspend fun triggerSyncByTaskId(taskId: String) {
        val task = taskDao.getTask(taskId) ?: return
        scheduleSync(task.goalId)
    }

    private suspend fun scheduleSync(goalId: String) {
        val goal = goalDao.getGoal(goalId) ?: return
        val profile = profileDao.getProfile(goal.profileId) ?: return

        enqueueWorker(profile.userId)
    }

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