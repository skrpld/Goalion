package com.skrpld.goalion.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * User data access object.
 * Handles CRUD operations for user entities in local database.
 */
@Dao
interface UserDao {
    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun delete(userId: String)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: String): UserEntity?
}

/**
 * Profile data access object.
 * Handles CRUD operations for profile entities with sync and soft-delete capabilities.
 */
@Dao
interface ProfileDao {
    @Upsert
    suspend fun upsert(profile: ProfileEntity)

    @Upsert
    suspend fun upsertAll(profiles: List<ProfileEntity>)

    @Query("UPDATE profiles SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :profileId")
    suspend fun softDelete(profileId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM profiles WHERE id = :profileId")
    suspend fun hardDelete(profileId: String)

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfile(id: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE userId = :userId AND isDeleted = 0")
    suspend fun getProfilesByUser(userId: String): List<ProfileEntity>

    @Query("SELECT * FROM profiles WHERE isSynced = 0")
    suspend fun getUnsynced(): List<ProfileEntity>

    @Query("UPDATE profiles SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT MAX(updatedAt) FROM profiles WHERE userId = :userId")
    suspend fun getLastUpdateTime(userId: String): Long?
}

/**
 * Goal data access object.
 * Handles CRUD operations for goal entities with sync and soft-delete capabilities.
 * Includes transactional query to fetch goals with their associated tasks.
 */
@Dao
interface GoalDao {
    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Upsert
    suspend fun upsertAll(goals: List<GoalEntity>)

    @Query("UPDATE goals SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :goalId")
    suspend fun softDelete(goalId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun hardDelete(goalId: String)

    @Query("UPDATE goals SET status = :status, updatedAt = :ts, isSynced = 0 WHERE id = :id")
    suspend fun updateStatus(id: String, status: Boolean, ts: Long = System.currentTimeMillis())

    @Query("UPDATE goals SET priority = :priority, updatedAt = :ts, isSynced = 0 WHERE id = :id")
    suspend fun updatePriority(id: String, priority: Int, ts: Long = System.currentTimeMillis())

    @Query("UPDATE goals SET `order` = :order, updatedAt = :ts, isSynced = 0 WHERE id = :id")
    suspend fun updateOrder(id: String, order: Int, ts: Long = System.currentTimeMillis())

    @Query("SELECT * FROM goals WHERE isSynced = 0")
    suspend fun getUnsynced(): List<GoalEntity>

    @Query("UPDATE goals SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoal(id: String): GoalEntity?

    @Query("SELECT MAX(updatedAt) FROM goals WHERE profileId IN (:profileIds)")
    suspend fun getLastUpdateTime(profileIds: List<String>): Long?

    @Query("SELECT id FROM goals WHERE profileId IN (:profileIds) AND isDeleted = 0")
    suspend fun getGoalIdsByProfileIds(profileIds: List<String>): List<String>

    @Transaction
    @Query("SELECT * FROM goals WHERE profileId = :profileId ORDER BY `order` ASC")
    fun getGoalsWithTasksList(profileId: String): Flow<List<GoalWithTasks>>
}

/**
 * Task data access object.
 * Handles CRUD operations for task entities with sync and soft-delete capabilities.
 */
@Dao
interface TaskDao {
    @Upsert
    suspend fun upsert(task: TaskEntity)

    @Upsert
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Query("UPDATE tasks SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :taskId")
    suspend fun softDelete(taskId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun hardDelete(taskId: String)

    @Query("UPDATE tasks SET status = :status, updatedAt = :ts, isSynced = 0 WHERE id = :id")
    suspend fun updateStatus(id: String, status: Boolean, ts: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET priority = :priority, updatedAt = :ts, isSynced = 0 WHERE id = :id")
    suspend fun updatePriority(id: String, priority: Int, ts: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET `order` = :order, updatedAt = :ts, isSynced = 0 WHERE id = :id")
    suspend fun updateOrder(id: String, order: Int, ts: Long = System.currentTimeMillis())

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TaskEntity>

    @Query("UPDATE tasks SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT MAX(updatedAt) FROM tasks WHERE goalId IN (:goalIds)")
    suspend fun getLastUpdateTime(goalIds: List<String>): Long?
}