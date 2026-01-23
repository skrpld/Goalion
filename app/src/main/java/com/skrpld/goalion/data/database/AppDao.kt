package com.skrpld.goalion.data.database

import androidx.room.*
import com.skrpld.goalion.data.models.*
import kotlinx.coroutines.flow.Flow

// TODO: Сделать синхронизацию удаления
@Dao
interface AppDao {
    // --- Profile ---
    @Upsert
    suspend fun upsertProfile(profile: Profile): Long

    @Delete
    suspend fun deleteProfile(profile: Profile)

    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getAnyProfile(): Profile?

    // --- Goal ---
    @Upsert
    suspend fun upsertGoal(goal: Goal): Long

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("""
        UPDATE goals
        SET status = :status, updatedAt = :timestamp, isSynced = 0
        WHERE id = :goalId
    """)
    suspend fun updateGoalStatus(goalId: Int, status: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE goals
        SET priority = :priority, updatedAt = :timestamp, isSynced = 0
        WHERE id = :goalId
    """)
    suspend fun updateGoalPriority(goalId: Int, priority: Int, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE goals
        SET order = :order, updatedAt = :timestamp, isSynced = 0
        WHERE id = :goalId
    """)
    suspend fun updateGoalOrder(goalId: Int, order: Int, timestamp: Long = System.currentTimeMillis())

    @Transaction
    @Query("""
        SELECT * FROM goals 
        WHERE profileId = :profileId 
        ORDER BY status ASC, priority ASC, updatedAt DESC
    """)
    fun getGoalsWithTasksList(profileId: Int): Flow<List<GoalWithTasks>>

    // --- Task ---
    @Upsert
    suspend fun upsertTask(task: Task): Long

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("""
        UPDATE goals
        SET status = :status, updatedAt = :timestamp, isSynced = 0
        WHERE id = :taskId
    """)
    suspend fun updateTaskStatus(taskId: Int, status: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE tasks
        SET priority = :priority, updatedAt = :timestamp, isSynced = 0
        WHERE id = :taskId
    """)
    suspend fun updateTaskPriority(taskId: Int, priority: Int, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE tasks
        SET order = :order, updatedAt = :timestamp, isSynced = 0
        WHERE id = :taskId
    """)
    suspend fun updateTaskOrder(taskId: Int, order: Int, timestamp: Long = System.currentTimeMillis())
}