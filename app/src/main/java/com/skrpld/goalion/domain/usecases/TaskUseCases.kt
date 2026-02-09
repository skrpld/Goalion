package com.skrpld.goalion.domain.usecases

import com.skrpld.goalion.domain.entities.Task
import com.skrpld.goalion.domain.repositories.TaskRepository

data class TaskInteractors(
    val create: CreateTaskUseCase,
    val update: UpdateTaskUseCase,
    val delete: DeleteTaskUseCase,
    val sync: SyncTaskUseCase,

    val updateStatus: UpdateTaskStatusUseCase,
    val updatePriority: UpdateTaskPriorityUseCase,
    val updateOrder: UpdateTaskOrderUseCase,
    val updateTitle: UpdateTaskTitleUseCase,
    val updateDescription: UpdateTaskDescriptionUseCase,
    val updateStartDate: UpdateTaskStartDateUseCase,
    val updateTargetDate: UpdateTaskTargetDateUseCase
)

/**
 * Creates a new task.
 * Generates a new ID and saves the task to repository.
 */
class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(goalId: String, title: String, description: String) {
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val task = Task(
            goalId = goalId,
            title = title,
            description = description
        )

        taskRepository.upsert(task)
    }
}

class UpdateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        id: String,
        goalId: String,
        title: String,
        description: String,
        status: Boolean,
        priority: Int,
        order: Int,
        startDate: Long,
        targetDate: Long
    ) {
        if (id.isBlank()) throw IllegalArgumentException("Task ID cannot be empty for update")

        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val task = Task(
            id = id,
            goalId = goalId,
            title = title,
            description = description,
            status = status,
            priority = priority,
            order = order,
            startDate = startDate,
            targetDate = targetDate
        )

        taskRepository.upsert(task)
    }
}

class DeleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) {
        taskRepository.delete(taskId)
    }
}

class SyncTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(goalId: String) {
        taskRepository.sync(goalId)
    }
}

class UpdateTaskStatusUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, status: Boolean) {
        taskRepository.updateStatus(taskId, status)
    }
}

class UpdateTaskPriorityUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, priority: Int) {
        taskRepository.updatePriority(taskId, priority)
    }
}

class UpdateTaskOrderUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, order: Int) {
        taskRepository.updateOrder(taskId, order)
    }
}

class UpdateTaskTitleUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, title: String) {
        if (taskId.isBlank()) throw IllegalArgumentException("Task ID cannot be empty")
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        taskRepository.updateTitle(taskId, title)
    }
}

class UpdateTaskDescriptionUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, description: String) {
        if (taskId.isBlank()) throw IllegalArgumentException("Task ID cannot be empty")

        taskRepository.updateDescription(taskId, description)
    }
}

class UpdateTaskStartDateUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, startDate: Long) {
        if (taskId.isBlank()) throw IllegalArgumentException("Task ID cannot be empty")

        taskRepository.updateStartDate(taskId, startDate)
    }
}

class UpdateTaskTargetDateUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, targetDate: Long) {
        if (taskId.isBlank()) throw IllegalArgumentException("Task ID cannot be empty")

        taskRepository.updateTargetDate(taskId, targetDate)
    }
}