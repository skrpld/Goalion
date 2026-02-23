package com.skrpld.goalion.domain.usecases

import android.util.Log
import com.skrpld.goalion.domain.model.Task
import com.skrpld.goalion.domain.repositories.TaskRepository

private const val TAG = "GoalionLog_TaskUC"

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
        Log.d(TAG, "Creating Task in Goal ID: $goalId. Title: $title")
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val task = Task(goalId = goalId, title = title, description = description)
        taskRepository.upsert(task)
    }
}

class UpdateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        id: String, goalId: String, title: String, description: String,
        status: Boolean, priority: Int, order: Int, startDate: Long, targetDate: Long
    ) {
        Log.d(TAG, "Updating Task ID: $id")
        if (id.isBlank()) throw IllegalArgumentException("Task ID cannot be empty for update")
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val task = Task(
            id = id, goalId = goalId, title = title, description = description,
            status = status, priority = priority, order = order,
            startDate = startDate, targetDate = targetDate
        )
        taskRepository.upsert(task)
    }
}

class DeleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) {
        Log.w(TAG, "Deleting Task ID: $taskId")
        taskRepository.delete(taskId)
    }
}

class SyncTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(goalId: String) {
        Log.d(TAG, "[SYNC] Forcing sync for Goal ID: $goalId via TaskUseCase")
        taskRepository.sync(goalId)
    }
}

class UpdateTaskStatusUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, status: Boolean) {
        Log.d(TAG, "Updating Task Status. ID: $taskId, New Status: $status")
        taskRepository.updateStatus(taskId, status)
    }
}

class UpdateTaskPriorityUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, priority: Int) {
        Log.d(TAG, "Updating Task Priority. ID: $taskId, New Priority: $priority")
        taskRepository.updatePriority(taskId, priority)
    }
}

class UpdateTaskOrderUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, order: Int) {
        Log.d(TAG, "Updating Task Order. ID: $taskId, New Order: $order")
        taskRepository.updateOrder(taskId, order)
    }
}

class UpdateTaskTitleUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, title: String) {
        Log.d(TAG, "Updating Task Title. ID: $taskId")
        if (taskId.isBlank()) throw IllegalArgumentException("Task ID cannot be empty")
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")
        taskRepository.updateTitle(taskId, title)
    }
}

class UpdateTaskDescriptionUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, description: String) {
        Log.d(TAG, "Updating Task Description. ID: $taskId")
        if (taskId.isBlank()) throw IllegalArgumentException("Task ID cannot be empty")
        taskRepository.updateDescription(taskId, description)
    }
}

class UpdateTaskStartDateUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, startDate: Long) {
        Log.d(TAG, "Updating Task Start Date. ID: $taskId")
        if (taskId.isBlank()) throw IllegalArgumentException("Task ID cannot be empty")
        taskRepository.updateStartDate(taskId, startDate)
    }
}

class UpdateTaskTargetDateUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, targetDate: Long) {
        Log.d(TAG, "Updating Task Target Date. ID: $taskId")
        if (taskId.isBlank()) throw IllegalArgumentException("Task ID cannot be empty")
        taskRepository.updateTargetDate(taskId, targetDate)
    }
}