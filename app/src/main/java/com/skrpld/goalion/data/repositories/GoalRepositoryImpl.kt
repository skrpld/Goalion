package com.skrpld.goalion.data.repositories

import androidx.work.*
import com.skrpld.goalion.data.local.GoalDao
import com.skrpld.goalion.data.local.ProfileDao
import com.skrpld.goalion.data.mappers.toDomain
import com.skrpld.goalion.data.mappers.toEntity
import com.skrpld.goalion.data.workers.SyncWorker
import com.skrpld.goalion.domain.Goal
import com.skrpld.goalion.domain.GoalWithTasks
import com.skrpld.goalion.domain.repositories.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepositoryImpl(
    private val goalDao: GoalDao,
    private val profileDao: ProfileDao,
    private val workManager: WorkManager
) : GoalRepository {

    override fun getGoalsWithTasks(profileId: String): Flow<List<GoalWithTasks>> {
        return goalDao.getGoalsWithTasksList(profileId)
            .map { list -> list.toDomain() }
    }

    override suspend fun upsertGoal(goal: Goal) {
        goalDao.upsert(goal.toEntity())
        scheduleSync(goal.profileId)
    }

    override suspend fun deleteGoal(goalId: String) {
        val goal = goalDao.getGoal(goalId)
        goalDao.softDelete(goalId)
        goal?.let { scheduleSync(it.profileId) }
    }

    override suspend fun updateGoalStatus(goalId: String, status: Boolean) {
        goalDao.updateStatus(goalId, status)
        triggerSyncByGoalId(goalId)
    }

    override suspend fun updateGoalPriority(goalId: String, priority: Int) {
        goalDao.updatePriority(goalId, priority)
        triggerSyncByGoalId(goalId)
    }

    override suspend fun updateGoalOrder(goalId: String, order: Int) {
        goalDao.updateOrder(goalId, order)
        triggerSyncByGoalId(goalId)
    }

    override suspend fun syncGoal(profileId: String) {
        scheduleSync(profileId)
    }

    /**
     * === Sync ===
     */
    private suspend fun triggerSyncByGoalId(goalId: String) {
        val goal = goalDao.getGoal(goalId) ?: return
        scheduleSync(goal.profileId)
    }

    private suspend fun scheduleSync(profileId: String) {
        val profile = profileDao.getProfile(profileId) ?: return
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