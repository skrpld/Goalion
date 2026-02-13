package com.skrpld.goalion.domain.usecases

import com.skrpld.goalion.domain.model.Goal
import com.skrpld.goalion.domain.model.GoalWithTasks
import com.skrpld.goalion.domain.repositories.GoalRepository
import kotlinx.coroutines.flow.Flow

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

/**
 * Observes goals with their tasks for a profile.
 * Returns a Flow that emits updated goal-task lists.
 */
class GetGoalsWithTasksUseCase(
    private val goalRepository: GoalRepository
) {
    operator fun invoke(profileId: String): Flow<List<GoalWithTasks>> {
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
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val goal = Goal(
            profileId = profileId,
            title = title,
            description = description
        )

        goalRepository.upsert(goal)
    }
}

class UpdateGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(
        id: String,
        profileId: String,
        title: String,
        description: String,
        status: Boolean,
        priority: Int,
        order: Int,
        startDate: Long,
        targetDate: Long
    ) {
        if (id.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty for update")

        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val goal = Goal(
            id = id,
            profileId = profileId,
            title = title,
            description = description,
            status = status,
            priority = priority,
            order = order,
            startDate = startDate,
            targetDate = targetDate
        )

        goalRepository.upsert(goal)
    }
}

class DeleteGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String) {
        goalRepository.delete(goalId)
    }
}

class SyncGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(profileId: String) {
        goalRepository.sync(profileId)
    }
}

class UpdateGoalStatusUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, status: Boolean) {
        goalRepository.updateStatus(goalId, status)
    }
}

class UpdateGoalPriorityUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, priority: Int) {
        goalRepository.updatePriority(goalId, priority)
    }
}

class UpdateGoalOrderUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, order: Int) {
        goalRepository.updateOrder(goalId, order)
    }
}

class UpdateGoalTitleUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, title: String) {
        if (goalId.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty")
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        goalRepository.updateTitle(goalId, title)
    }
}

class UpdateGoalDescriptionUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, description: String) {
        if (goalId.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty")

        goalRepository.updateDescription(goalId, description)
    }
}

class UpdateGoalStartDateUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, startDate: Long) {
        if (goalId.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty")

        goalRepository.updateStartDate(goalId, startDate)
    }
}

class UpdateGoalTargetDateUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, targetDate: Long) {
        if (goalId.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty")

        goalRepository.updateTargetDate(goalId, targetDate)
    }
}