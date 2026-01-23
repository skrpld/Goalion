package com.skrpld.goalion.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// TODO: Сделать синхронизацию удаления
@Dao
interface UserDao {
    @Upsert
    suspend fun upsertUser(user: UserEntity): Long

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface ProfileDao {
    @Upsert
    suspend fun upsertProfile(profile: ProfileEntity): Long

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getAnyProfile(): ProfileEntity?
}

@Dao
interface GoalDao {
    @Upsert
    suspend fun upsertGoal(goal: GoalEntity): Long

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query(
        """
        UPDATE goals
        SET status = :status, updatedAt = :timestamp, isSynced = 0
        WHERE id = :goalId
    """
    )
    suspend fun updateGoalStatus(
        goalId: Int,
        status: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE goals
        SET priority = :priority, updatedAt = :timestamp, isSynced = 0
        WHERE id = :goalId
    """
    )
    suspend fun updateGoalPriority(
        goalId: Int,
        priority: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE goals
        SET order = :order, updatedAt = :timestamp, isSynced = 0
        WHERE id = :goalId
    """
    )
    suspend fun updateGoalOrder(
        goalId: Int,
        order: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Transaction
    @Query(
        """
        SELECT * FROM goals 
        WHERE profileId = :profileId 
        ORDER BY status ASC, priority ASC, updatedAt DESC
    """
    )
    fun getGoalsWithTasksList(profileId: Int): Flow<List<GoalWithTasks>>
}

@Dao
interface TaskDao {
    @Upsert
    suspend fun upsertTask(task: TaskEntity): Long

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("""
        UPDATE goals
        SET status = :status, updatedAt = :timestamp, isSynced = 0
        WHERE id = :taskId
    """)
    suspend fun updateTaskStatus(
        taskId: Int,
        status: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE tasks
        SET priority = :priority, updatedAt = :timestamp, isSynced = 0
        WHERE id = :taskId
    """)
    suspend fun updateTaskPriority(
        taskId: Int,
        priority: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE tasks
        SET order = :order, updatedAt = :timestamp, isSynced = 0
        WHERE id = :taskId
    """)
    suspend fun updateTaskOrder(
        taskId: Int,
        order: Int,
        timestamp: Long = System.currentTimeMillis()
    )
}