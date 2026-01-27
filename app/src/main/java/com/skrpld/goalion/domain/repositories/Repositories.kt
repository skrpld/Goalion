package com.skrpld.goalion.domain.repositories

import com.skrpld.goalion.domain.Goal
import com.skrpld.goalion.domain.GoalWithTasks
import com.skrpld.goalion.domain.Profile
import com.skrpld.goalion.domain.Task
import com.skrpld.goalion.domain.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): String?

    suspend fun signUp(name: String, email: String, pass: String): Result<User>
    suspend fun signIn(email: String, pass: String): Result<User>
    suspend fun logout()
}

interface UserRepository {
    suspend fun getUser(userId: String): User?

    suspend fun upsertUser(user: User)
    suspend fun deleteUser(userId: String)
}

interface ProfileRepository {
    suspend fun getProfilesByUser(userId: String): List<Profile>

    suspend fun upsertProfile(profile: Profile)
    suspend fun deleteProfile(profileId: String)

    suspend fun syncProfile(userId: String)
    suspend fun fetchProfiles(userId: String)
}

interface GoalRepository {
    fun getGoalsWithTasks(profileId: String): Flow<List<GoalWithTasks>>

    suspend fun upsertGoal(goal: Goal)
    suspend fun deleteGoal(goalId: String)

    suspend fun updateGoalStatus(goalId: String, status: Boolean)
    suspend fun updateGoalPriority(goalId: String, priority: Int)
    suspend fun updateGoalOrder(goalId: String, order: Int)

    suspend fun syncGoal(profileId: String)
    suspend fun fetchGoals(userId: String)
}

interface TaskRepository {
    suspend fun upsertTask(task: Task)
    suspend fun deleteTask(taskId: String)

    suspend fun updateTaskStatus(taskId: String, status: Boolean)
    suspend fun updateTaskPriority(taskId: String, priority: Int)
    suspend fun updateTaskOrder(taskId: String, order: Int)

    suspend fun syncTask(goalId: String)
    suspend fun fetchTasks(userId: String)
}