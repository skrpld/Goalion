package com.skrpld.goalion.data.database

import androidx.room.*
import com.skrpld.goalion.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Profile ---
    @Upsert
    suspend fun upsertProfile(profile: Profile): Long

    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getAnyProfile(): Profile?

    // --- Goal ---
    @Upsert
    suspend fun upsertGoal(goal: Goal): Long

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Transaction
    @Query("""
        SELECT * FROM goals 
        WHERE profileId = :profileId 
        ORDER BY status ASC, priority ASC, orderIndex ASC
    """)
    fun getGoalsWithTasksList(profileId: Int): Flow<List<GoalWithTasks>>

    // --- Task ---
    @Upsert
    suspend fun upsertTask(task: Task): Long

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE goals SET status = :status WHERE id = :goalId")
    suspend fun updateGoalStatus(goalId: Int, status: TaskStatus)

    @Query("UPDATE goals SET priority = :priority WHERE id = :goalId")
    suspend fun updateGoalPriority(goalId: Int, priority: Int)

    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, status: TaskStatus)

    @Query("UPDATE tasks SET priority = :priority WHERE id = :taskId")
    suspend fun updateTaskPriority(taskId: Int, priority: Int)
}