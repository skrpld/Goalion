package com.skrpld.goalion.domain.repositories

import com.skrpld.goalion.domain.entities.Goal
import com.skrpld.goalion.domain.entities.GoalWithTasks
import com.skrpld.goalion.domain.entities.Profile
import com.skrpld.goalion.domain.entities.Task
import com.skrpld.goalion.domain.entities.User
import kotlinx.coroutines.flow.Flow

/**
 * Authentication repository.
 */
interface AuthRepository {
    fun getCurrentUser(): String?
    suspend fun signUp(name: String, email: String, pass: String): Result<User>
    suspend fun signIn(email: String, pass: String): Result<User>
    suspend fun logout()
    suspend fun upsertUser(user: User)
    suspend fun reLoginAndRetry(email: String, pass: String, userToSave: User)
    suspend fun changePassword(currentPass: String, newPass: String): Result<Unit>
}

/**
 * User repository.
 */
interface UserRepository {
    suspend fun getUser(userId: String): User?
    suspend fun upsert(user: User)
    suspend fun delete(userId: String)
    suspend fun isNameTaken(name: String): Boolean
    suspend fun isEmailTaken(email: String): Boolean
}

/**
 * Profile repository.
 */
interface ProfileRepository {
    suspend fun getProfilesByUser(userId: String): List<Profile>
    suspend fun upsert(profile: Profile)
    suspend fun delete(profileId: String)
    suspend fun sync(userId: String)
}

/**
 * Goal repository.
 */
interface GoalRepository {
    fun getGoalsWithTasks(profileId: String): Flow<List<GoalWithTasks>>
    suspend fun upsert(goal: Goal)
    suspend fun delete(goalId: String)
    suspend fun updateTitle(goalId: String, title: String)
    suspend fun updateDescription(goalId: String, description: String)
    suspend fun updateStatus(goalId: String, status: Boolean)
    suspend fun updatePriority(goalId: String, priority: Int)
    suspend fun updateOrder(goalId: String, order: Int)
    suspend fun updateStartDate(goalId: String, startDate: Long)
    suspend fun updateTargetDate(goalId: String, targetDate: Long)
    suspend fun sync(profileId: String)
}

/**
 * Task repository.
 */
interface TaskRepository {
    suspend fun upsert(task: Task)
    suspend fun delete(taskId: String)
    suspend fun updateTitle(taskId: String, title: String)
    suspend fun updateDescription(taskId: String, description: String)
    suspend fun updateStatus(taskId: String, status: Boolean)
    suspend fun updatePriority(taskId: String, priority: Int)
    suspend fun updateOrder(taskId: String, order: Int)
    suspend fun updateStartDate(taskId: String, startDate: Long)
    suspend fun updateTargetDate(taskId: String, targetDate: Long)
    suspend fun sync(goalId: String)
}