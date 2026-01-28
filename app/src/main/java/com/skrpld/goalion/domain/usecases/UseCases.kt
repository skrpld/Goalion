package com.skrpld.goalion.domain.usecases

import com.skrpld.goalion.domain.entities.Goal
import com.skrpld.goalion.domain.entities.GoalWithTasks
import com.skrpld.goalion.domain.entities.Profile
import com.skrpld.goalion.domain.entities.Task
import com.skrpld.goalion.domain.entities.User
import com.skrpld.goalion.domain.repositories.AuthRepository
import com.skrpld.goalion.domain.repositories.GoalRepository
import com.skrpld.goalion.domain.repositories.ProfileRepository
import com.skrpld.goalion.domain.repositories.TaskRepository
import com.skrpld.goalion.domain.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * === Auth ===
 */
class SignUpUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    private val nameRegex = Regex("^[a-zA-Z0-9]{6,}$")
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    suspend operator fun invoke(name: String, email: String, pass: String): Result<User> {
        if (!name.matches(nameRegex)) {
            return Result.failure(IllegalArgumentException("Name must contain only English letters and digits, and be at least 6 characters long"))
        }

        if (!email.matches(emailRegex)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        if (pass.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters long"))
        }

        return try {
            if (userRepository.isNameTaken(name)) {
                return Result.failure(IllegalArgumentException("Username '$name' is already taken"))
            }

            if (userRepository.isEmailTaken(email)) {
                return Result.failure(IllegalArgumentException("Email '$email' is already in use"))
            }

            authRepository.signUp(name, email, pass)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class SignInUseCase(
    private val authRepository: AuthRepository
) {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    suspend operator fun invoke(email: String, pass: String): Result<User> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }

        if (!email.matches(emailRegex)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        if (pass.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }

        return authRepository.signIn(email, pass)
    }
}

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}

/**
 * === User ===
 */
class GetUserUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): User? {
        val userId = authRepository.getCurrentUser() ?: return null
        return userRepository.getUser(userId)
    }
}

class UpdateUserUseCase(
    private val userRepository: UserRepository
) {
    private val nameRegex = Regex("^[a-zA-Z0-9]{6,}$")
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    suspend operator fun invoke(id: String, name: String, email: String) {
        if (id.isBlank()) throw IllegalArgumentException("User ID cannot be empty")

        if (!name.matches(nameRegex)) {
            throw IllegalArgumentException("Name must contain only English letters and digits, and be at least 6 characters long")
        }

        if (!email.matches(emailRegex)) {
            throw IllegalArgumentException("Invalid email format")
        }

        val currentUser = userRepository.getUser(id)
            ?: throw IllegalStateException("User not found locally")

        if (currentUser.name != name) {
            val isNameTaken = userRepository.isNameTaken(name)
            if (isNameTaken) {
                throw IllegalArgumentException("Username '$name' is already taken")
            }
        }

        if (currentUser.email != email) {
            val isEmailTaken = userRepository.isEmailTaken(email)
            if (isEmailTaken) {
                throw IllegalArgumentException("Email '$email' is already in use")
            }
        }

        val updatedUser = User(
            id = id,
            name = name,
            email = email
        )

        userRepository.upsertUser(updatedUser)
    }
}

class DeleteUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String) {
        userRepository.deleteUser(userId)
    }
}

/**
 * === Profile ===
 */
class GetProfilesUseCases(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String): List<Profile> {
        return profileRepository.getProfilesByUser(userId)
    }
}

class CreateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String, title: String, description: String) {
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val newProfileId = UUID.randomUUID().toString()

        val profile = Profile(
            id = newProfileId,
            userId = userId,
            title = title,
            description = description
        )

        profileRepository.upsertProfile(profile)
    }
}

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(id: String, userId: String, title: String, description: String) {
        if (id.isBlank()) throw IllegalArgumentException("Profile ID cannot be empty for update")

        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val profile = Profile(
            id = id,
            userId = userId,
            title = title,
            description = description
        )

        profileRepository.upsertProfile(profile)
    }
}

class DeleteProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profileId: String) {
        profileRepository.deleteProfile(profileId)
    }
}

class SyncProfilesUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String) {
        profileRepository.syncProfiles(userId)
    }
}

/**
 * === Goal ===
 */
class GetGoalsWithTasksUseCase(
    private val goalRepository: GoalRepository
) {
    operator fun invoke(profileId: String): Flow<List<GoalWithTasks>> {
        return goalRepository.getGoalsWithTasks(profileId)
    }
}

class CreateGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(profileId: String, title: String, description: String) {
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val newGoalId = UUID.randomUUID().toString()

        val goal = Goal(
            id = newGoalId,
            profileId = profileId,
            title = title,
            description = description
        )

        goalRepository.upsertGoal(goal)
    }
}

class UpdateGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(id: String, profileId: String, title: String, description: String) {
        if (id.isBlank()) throw IllegalArgumentException("Goal ID cannot be empty for update")

        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val goal = Goal(
            id = id,
            profileId = profileId,
            title = title,
            description = description
        )

        goalRepository.upsertGoal(goal)
    }
}

class DeleteGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String) {
        goalRepository.deleteGoal(goalId)
    }
}

class UpdateGoalStatusUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, status: Boolean) {
        goalRepository.updateGoalStatus(goalId, status)
    }
}

class UpdateGoalPriorityUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, priority: Int) {
        goalRepository.updateGoalPriority(goalId, priority)
    }
}

class UpdateGoalOrderUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, order: Int) {
        goalRepository.updateGoalOrder(goalId, order)
    }
}

class SyncGoalUseCase(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(profileId: String) {
        goalRepository.syncGoal(profileId)
    }
}

/**
 * === Task ===
 */
class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(goalId: String, title: String, description: String) {
        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val newTaskId = UUID.randomUUID().toString()

        val task = Task(
            id = newTaskId,
            goalId = goalId,
            title = title,
            description = description
        )

        taskRepository.upsertTask(task)
    }
}

class UpdateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(id: String, goalId: String, title: String, description: String) {
        if (id.isBlank()) throw IllegalArgumentException("Task ID cannot be empty for update")

        if (title.isBlank()) throw IllegalArgumentException("Title cannot be empty")

        val task = Task(
            id = id,
            goalId = goalId,
            title = title,
            description = description
        )

        taskRepository.upsertTask(task)
    }
}

class DeleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) {
        taskRepository.deleteTask(taskId)
    }
}

class UpdateTaskStatusUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, status: Boolean) {
        taskRepository.updateTaskStatus(taskId, status)
    }
}

class UpdateTaskPriorityUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, priority: Int) {
        taskRepository.updateTaskPriority(taskId, priority)
    }
}

class UpdateTaskOrderUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, order: Int) {
        taskRepository.updateTaskOrder(taskId, order)
    }
}

class SyncTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(goalId: String) {
        taskRepository.syncTask(goalId)
    }
}