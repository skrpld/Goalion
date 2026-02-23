package com.skrpld.goalion.domain.usecases

import android.util.Log
import com.skrpld.goalion.domain.model.Goal
import com.skrpld.goalion.domain.model.GoalWithTasks
import com.skrpld.goalion.domain.repositories.GoalRepository
import kotlinx.coroutines.flow.Flow

private const val TAG = "GoalionLog_GoalUC"

data class GoalInteractors(
    val getWithTasks: GetGoalsWithTasksUseCase,
    val create: CreateGoalUseCase,
    val update: UpdateGoalUseCase,
    val delete: DeleteGoalUseCase,
    val sync: SyncGoalUseCase,

    val updateStatus: UpdateGoalStatusUseCase,
    val updatePriority: UpdateGoalPriorityUseCase,
    val updateOrder: UpdateGoalOrderUseCase,
    val updateTitle: UpdateGoalTitleUseCase,
    val updateDescription: UpdateGoalDescriptionUseCase,
    val updateStartDate: UpdateGoalStartDateUseCase,
    val updateTargetDate: UpdateGoalTargetDateUseCase
)

class GetGoalsWithTasksUseCase(
    private val goalRepository: GoalRepository
) {
    operator fun invoke(profileId: String): Flow<List<GoalWithTasks>> {
        Log.d(TAG, "Requesting Flow of GoalsWithTasks for Profile ID: $profileId")
        return goalRepository.getGoalsWithTasks(profileId)
    }
}

/**
 * Creates a new goal.
 * Generates a new ID and saves the goal to repository.
 */
class CreateGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(profileId: String, title: String, description: String) {
        Log.d(TAG, "Creating Goal in Profile ID: $profileId. Title: $title")
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val goal = Goal(profileId = profileId, title = title, description = description)
        goalRepository.upsert(goal)
    }
}

class UpdateGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(
        id: String, profileId: String, title: String, description: String,
        status: Boolean, priority: Int, order: Int, startDate: Long, targetDate: Long
    ) {
        Log.d(TAG, "Updating Goal ID: $id")
        if (id.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty for update")
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val goal = Goal(
            id = id, profileId = profileId, title = title, description = description,
            status = status, priority = priority, order = order,
            startDate = startDate, targetDate = targetDate
        )
        goalRepository.upsert(goal)
    }
}

class DeleteGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String) {
        Log.w(TAG, "Deleting Goal ID: $goalId")
        goalRepository.delete(goalId)
    }
}

class SyncGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(profileId: String) {
        Log.d(TAG, "[SYNC] Forcing sync for Profile ID: $profileId via GoalUseCase")
        goalRepository.sync(profileId)
    }
}

class UpdateGoalStatusUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, status: Boolean) {
        Log.d(TAG, "Updating Goal Status. ID: $goalId, New Status: $status")
        goalRepository.updateStatus(goalId, status)
    }
}

class UpdateGoalPriorityUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, priority: Int) {
        Log.d(TAG, "Updating Goal Priority. ID: $goalId, New Priority: $priority")
        goalRepository.updatePriority(goalId, priority)
    }
}

class UpdateGoalOrderUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, order: Int) {
        Log.d(TAG, "Updating Goal Order. ID: $goalId, New Order: $order")
        goalRepository.updateOrder(goalId, order)
    }
}

class UpdateGoalTitleUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, title: String) {
        Log.d(TAG, "Updating Goal Title. ID: $goalId")
        if (goalId.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty")
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")
        goalRepository.updateTitle(goalId, title)
    }
}

class UpdateGoalDescriptionUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, description: String) {
        Log.d(TAG, "Updating Goal Description. ID: $goalId")
        if (goalId.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty")
        goalRepository.updateDescription(goalId, description)
    }
}

class UpdateGoalStartDateUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, startDate: Long) {
        Log.d(TAG, "Updating Goal Start Date. ID: $goalId")
        if (goalId.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty")
        goalRepository.updateStartDate(goalId, startDate)
    }
}

class UpdateGoalTargetDateUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, targetDate: Long) {
        Log.d(TAG, "Updating Goal Target Date. ID: $goalId")
        if (goalId.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty")
        goalRepository.updateTargetDate(goalId, targetDate)
    }
}