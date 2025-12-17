package com.skrpld.goalion.data.database

import androidx.room.*
import com.skrpld.goalion.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Profile ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile): Long

    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getAnyProfile(): Profile?

    // --- Goal ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Transaction
    @Query("SELECT * FROM goals WHERE profileId = :profileId ORDER BY id DESC")
    fun getGoalsWithTasksList(profileId: Int): Flow<List<GoalWithTasks>>

    @Query("UPDATE goals SET status = :status WHERE id = :goalId")
    suspend fun updateGoalStatus(goalId: Int, status: TaskStatus)

    @Query("UPDATE goals SET priority = :priority WHERE id = :goalId")
    suspend fun updateGoalPriority(goalId: Int, priority: TaskPriority)

    // --- Task ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, status: TaskStatus)


    @Query("UPDATE tasks SET priority = :priority WHERE id = :taskId")
    suspend fun updateTaskPriority(taskId: Int, priority: TaskPriority)
}