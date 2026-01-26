package com.skrpld.goalion.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Upsert
    suspend fun upsert(user: UserEntity): Long

    @Delete
    suspend fun delete(user: UserEntity)
}

@Dao
interface ProfileDao {
    @Upsert
    suspend fun upsert(profile: ProfileEntity): Long

    @Delete
    suspend fun delete(profile: ProfileEntity)

    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getAny(): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE isSynced = 0")
    suspend fun getUnsynced(): List<ProfileEntity>

    @Query("UPDATE profiles SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT MAX(updatedAt) FROM profiles")
    suspend fun getLastUpdateTime(): Long?
}

@Dao
interface GoalDao {
    @Upsert
    suspend fun upsert(goal: GoalEntity): Long

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("""
        UPDATE goals
        SET status = :status, updatedAt = :timestamp, isSynced = 0
        WHERE id = :goalId
    """
    )
    suspend fun updateStatus(
        goalId: String,
        status: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE goals
        SET priority = :priority, updatedAt = :timestamp, isSynced = 0
        WHERE id = :goalId
    """
    )
    suspend fun updatePriority(
        goalId: String,
        priority: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE goals
        SET 'order' = :order, updatedAt = :timestamp, isSynced = 0
        WHERE id = :goalId
    """
    )
    suspend fun updateOrder(
        goalId: String,
        order: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM goals WHERE isSynced = 0")
    suspend fun getUnsynced(): List<GoalEntity>

    @Query("UPDATE goals SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT MAX(updatedAt) FROM goals")
    suspend fun getLastUpdateTime(): Long?

    @Transaction
    @Query("""
        SELECT * FROM goals 
        WHERE profileId = :profileId 
        ORDER BY status ASC, priority ASC, updatedAt DESC
    """
    )
    fun getGoalsWithTasksList(profileId: String): Flow<List<GoalWithTasks>>
}

@Dao
interface TaskDao {
    @Upsert
    suspend fun upsert(task: TaskEntity): Long

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("""
        UPDATE goals
        SET status = :status, updatedAt = :timestamp, isSynced = 0
        WHERE id = :taskId
    """)
    suspend fun updateStatus(
        taskId: String,
        status: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE tasks
        SET priority = :priority, updatedAt = :timestamp, isSynced = 0
        WHERE id = :taskId
    """)
    suspend fun updatePriority(
        taskId: String,
        priority: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE tasks
        SET 'order' = :order, updatedAt = :timestamp, isSynced = 0
        WHERE id = :taskId
    """)
    suspend fun updateOrder(
        taskId: String,
        order: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TaskEntity>

    @Query("UPDATE tasks SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT MAX(updatedAt) FROM tasks")
    suspend fun getLastUpdateTime(): Long?
}