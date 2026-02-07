package com.skrpld.goalion.data.repositories

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.skrpld.goalion.data.local.GoalDao
import com.skrpld.goalion.data.local.ProfileDao
import com.skrpld.goalion.data.local.TaskDao
import com.skrpld.goalion.data.local.UserDao
import com.skrpld.goalion.data.mappers.toDomain
import com.skrpld.goalion.data.mappers.toEntity
import com.skrpld.goalion.data.mappers.toNetwork
import com.skrpld.goalion.data.remote.AuthRemoteDataSource
import com.skrpld.goalion.data.remote.UserRemoteDataSource
import com.skrpld.goalion.data.util.SyncUtil
import com.skrpld.goalion.data.workers.SyncWorker
import com.skrpld.goalion.domain.entities.GoalWithTasks
import com.skrpld.goalion.domain.entities.Goal
import com.skrpld.goalion.domain.entities.Profile
import com.skrpld.goalion.domain.entities.Task
import com.skrpld.goalion.domain.entities.User
import com.skrpld.goalion.domain.repositories.AuthRepository
import com.skrpld.goalion.domain.repositories.GoalRepository
import com.skrpld.goalion.domain.repositories.ProfileRepository
import com.skrpld.goalion.domain.repositories.TaskRepository
import com.skrpld.goalion.domain.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Authentication repository implementation.
 * Handles user authentication and manages sync operations after login.
 */
class AuthRepositoryImpl(
    private val authRemote: AuthRemoteDataSource,
    private val userRemote: UserRemoteDataSource,
    private val userDao: UserDao,
    private val workManager: WorkManager
) : AuthRepository {

    override fun getCurrentUser(): String? = authRemote.getCurrentUserId()

    override suspend fun signUp(name: String, email: String, pass: String): Result<User> {
        return try {
            val firebaseUser = authRemote.signUp(email, pass)
                ?: return Result.failure(Exception("Sign up failed"))

            val user = User(id = firebaseUser.uid, name = name, email = email)

            userRemote.upsertUser(user.toNetwork())
            userDao.upsert(user.toEntity())

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, pass: String): Result<User> {
        return try {
            val firebaseUser = authRemote.signIn(email, pass)
                ?: return Result.failure(Exception("Sign in failed"))

            val networkUser = userRemote.getUser(firebaseUser.uid)
            val domainUser = networkUser?.toDomain()
                ?: User(id = firebaseUser.uid, name = "Unknown", email = email)

            userDao.upsert(domainUser.toEntity())

            startSync(domainUser.id)

            Result.success(domainUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        authRemote.logout()
    }

    override suspend fun upsertUser(user: User) {
        val currentUser = authRemote.getCurrentUser()
            ?: throw IllegalStateException("User must be logged in")

        val oldEmail = currentUser.email
        val newEmail = user.email
        val isEmailChanged = oldEmail != newEmail && newEmail.isNotBlank()

        try {
            if (isEmailChanged) {
                authRemote.updateEmail(newEmail)
            }

            userRemote.upsertUser(user.toNetwork())
            userDao.upsert(user.toEntity())

        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            throw e
        } catch (e: FirebaseAuthUserCollisionException) {
            throw Exception("This Email is already in use")
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun reLoginAndRetry(email: String, pass: String, userToSave: User) {
        authRemote.reauthenticate(email, pass)
        upsertUser(userToSave)
    }

    override suspend fun changePassword(currentPass: String, newPass: String): Result<Unit> {
        return try {
            val user = authRemote.getCurrentUser()
                ?: return Result.failure(Exception("User not found"))
            val email = user.email
                ?: return Result.failure(Exception("User email not found"))

            authRemote.reauthenticate(email, currentPass)
            authRemote.updatePassword(newPass)

            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid current password"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Starts a sync operation for the specified user.
     *
     * @param userId The unique identifier of the user to sync
     */
    private fun startSync(userId: String) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("USER_ID" to userId))
            .build()
        workManager.enqueue(request)
    }
}

/**
 * User repository implementation.
 * Manages user data in local database and syncs with remote.
 */
class UserRepositoryImpl(
    private val userDao: UserDao,
    private val userRemote: UserRemoteDataSource
) : UserRepository {
    override suspend fun getUser(userId: String): User? {
        return userDao.getUser(userId)?.toDomain()
    }

    override suspend fun upsert(user: User) {
        userDao.upsert(user.toEntity())
        try {
            userRemote.upsertUser(user.toNetwork())
        } catch (e: Exception) {
            TODO("delayed sending")
        }
    }

    override suspend fun delete(userId: String) {
        userDao.delete(userId)
        userRemote.deleteUser(userId)
    }

    override suspend fun isNameTaken(name: String): Boolean {
        return userRemote.isNameTaken(name)
    }

    override suspend fun isEmailTaken(email: String): Boolean {
        return userRemote.isEmailTaken(email)
    }
}

/**
 * Profile repository implementation.
 * Manages profile data and triggers sync operations.
 */
class ProfileRepositoryImpl(
    private val profileDao: ProfileDao,
    private val workManager: WorkManager
) : ProfileRepository {

    override suspend fun getProfilesByUser(userId: String): List<Profile> {
        return profileDao.getProfilesByUser(userId).map { it.toDomain() }
    }

    override suspend fun upsert(profile: Profile) {
        profileDao.upsert(profile.toEntity())
        startSync(profile.userId)
    }

    override suspend fun delete(profileId: String) {
        profileDao.softDelete(profileId)
    }

    override suspend fun sync(userId: String) {
        startSync(userId)
    }

    /**
     * Starts a sync operation for the specified user.
     *
     * @param userId The unique identifier of the user to sync
     */
    private fun startSync(userId: String) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("USER_ID" to userId))
            .build()
        workManager.enqueue(request)
    }
}

/**
 * Goal repository implementation.
 * Manages goal data and handles cascading sync operations for profiles.
 */
class GoalRepositoryImpl(
    private val goalDao: GoalDao,
    private val profileDao: ProfileDao,
    private val workManager: WorkManager
) : GoalRepository {
    
    private val syncUtil = SyncUtil(goalDao, profileDao, workManager)

    override fun getGoalsWithTasks(profileId: String): Flow<List<GoalWithTasks>> {
        return goalDao.getGoalsWithTasksList(profileId)
            .map { list -> list.toDomain() }
    }

    override suspend fun upsert(goal: Goal) {
        goalDao.upsert(goal.toEntity())
        syncUtil.scheduleSyncByProfileId(goal.profileId)
    }

    override suspend fun delete(goalId: String) {
        val goal = goalDao.getGoal(goalId)
        goalDao.softDelete(goalId)
        goal?.let { syncUtil.scheduleSyncByProfileId(it.profileId) }
    }

    override suspend fun updateStatus(goalId: String, status: Boolean) {
        goalDao.updateStatus(goalId, status)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updatePriority(goalId: String, priority: Int) {
        goalDao.updatePriority(goalId, priority)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updateOrder(goalId: String, order: Int) {
        goalDao.updateOrder(goalId, order)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updateStartDate(goalId: String, startDate: Long) {
        goalDao.updateStartDate(goalId, startDate)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updateTitle(goalId: String, title: String) {
        goalDao.updateTitle(goalId, title)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updateDescription(goalId: String, description: String) {
        goalDao.updateDescription(goalId, description)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updateTargetDate(goalId: String, targetDate: Long) {
        goalDao.updateTargetDate(goalId, targetDate)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun sync(profileId: String) {
        syncUtil.scheduleSyncByProfileId(profileId)
    }
}

/**
 * Task repository implementation.
 * Manages task data and handles cascading sync operations for goals and profiles.
 */
class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val goalDao: GoalDao,
    private val profileDao: ProfileDao,
    private val workManager: WorkManager
) : TaskRepository {
    
    private val syncUtil = SyncUtil(goalDao, profileDao, workManager)

    override suspend fun upsert(task: Task) {
        taskDao.upsert(task.toEntity())
        syncUtil.scheduleSync(task.goalId)
    }

    override suspend fun delete(taskId: String) {
        val task = taskDao.getTask(taskId)
        taskDao.softDelete(taskId)
        task?.let { syncUtil.scheduleSync(it.goalId) }
    }

    override suspend fun updateStatus(taskId: String, status: Boolean) {
        taskDao.updateStatus(taskId, status)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updatePriority(taskId: String, priority: Int) {
        taskDao.updatePriority(taskId, priority)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updateOrder(taskId: String, order: Int) {
        taskDao.updateOrder(taskId, order)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updateStartDate(taskId: String, startDate: Long) {
        taskDao.updateStartDate(taskId, startDate)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updateTitle(taskId: String, title: String) {
        taskDao.updateTitle(taskId, title)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updateDescription(taskId: String, description: String) {
        taskDao.updateDescription(taskId, description)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updateTargetDate(taskId: String, targetDate: Long) {
        taskDao.updateTargetDate(taskId, targetDate)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun sync(goalId: String) {
        syncUtil.scheduleSync(goalId)
    }
}