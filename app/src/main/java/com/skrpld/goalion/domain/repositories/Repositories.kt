package com.skrpld.goalion.domain.repositories

import com.skrpld.goalion.domain.entities.Goal
import com.skrpld.goalion.domain.entities.GoalWithTasks
import com.skrpld.goalion.domain.entities.Profile
import com.skrpld.goalion.domain.entities.Task
import com.skrpld.goalion.domain.entities.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): String?

    suspend fun signUp(name: String, email: String, pass: String): Result<User>
    suspend fun signIn(email: String, pass: String): Result<User>
    suspend fun logout()
    suspend fun upsertUser(user: User)
    suspend fun reLoginAndRetry(email: String, pass: String, userToSave: User)
    suspend fun changePassword(currentPass: String, newPass: String): Result<Unit>
}

interface UserRepository {
    suspend fun getUser(userId: String): User?

    suspend fun upsertUser(user: User)
    suspend fun deleteUser(userId: String)

    suspend fun isNameTaken(name: String): Boolean
    suspend fun isEmailTaken(email: String): Boolean
}

interface ProfileRepository {
    suspend fun getProfilesByUser(userId: String): List<Profile>

    suspend fun upsertProfile(profile: Profile)
    suspend fun deleteProfile(profileId: String)

    suspend fun syncProfiles(userId: String)
}

interface GoalRepository {
    fun getGoalsWithTasks(profileId: String): Flow<List<GoalWithTasks>>

    suspend fun upsertGoal(goal: Goal)
    suspend fun deleteGoal(goalId: String)

    suspend fun updateGoalStatus(goalId: String, status: Boolean)
    suspend fun updateGoalPriority(goalId: String, priority: Int)
    suspend fun updateGoalOrder(goalId: String, order: Int)

    suspend fun syncGoal(profileId: String)
}

interface TaskRepository {
    suspend fun upsertTask(task: Task)
    suspend fun deleteTask(taskId: String)

    suspend fun updateTaskStatus(taskId: String, status: Boolean)
    suspend fun updateTaskPriority(taskId: String, priority: Int)
    suspend fun updateTaskOrder(taskId: String, order: Int)

    suspend fun syncTask(goalId: String)
}