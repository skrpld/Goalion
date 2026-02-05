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

    // TODO("Update Email on firebase Auth")
    override suspend fun upsertUser(user: User) {
        userDao.upsert(user.toEntity())
        try {
            userRemote.upsertUser(user.toNetwork())
        } catch (e: Exception) {
            TODO("delayed sending")
        }
    }

    override suspend fun deleteUser(userId: String) {
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

    override suspend fun upsertProfile(profile: Profile) {
        profileDao.upsert(profile.toEntity())
        startSync(profile.userId)
    }

    override suspend fun deleteProfile(profileId: String) {
        profileDao.softDelete(profileId)
    }

    override suspend fun syncProfiles(userId: String) {
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

    override fun getGoalsWithTasks(profileId: String): Flow<List<GoalWithTasks>> {
        return goalDao.getGoalsWithTasksList(profileId)
            .map { list -> list.toDomain() }
    }

    override suspend fun upsertGoal(goal: Goal) {
        goalDao.upsert(goal.toEntity())
        scheduleSync(goal.profileId)
    }

    override suspend fun deleteGoal(goalId: String) {
        val goal = goalDao.getGoal(goalId)
        goalDao.softDelete(goalId)
        goal?.let { scheduleSync(it.profileId) }
    }

    override suspend fun updateGoalStatus(goalId: String, status: Boolean) {
        goalDao.updateStatus(goalId, status)
        triggerSyncByGoalId(goalId)
    }

    override suspend fun updateGoalPriority(goalId: String, priority: Int) {
        goalDao.updatePriority(goalId, priority)
        triggerSyncByGoalId(goalId)
    }

    override suspend fun updateGoalOrder(goalId: String, order: Int) {
        goalDao.updateOrder(goalId, order)
        triggerSyncByGoalId(goalId)
    }

    override suspend fun syncGoal(profileId: String) {
        scheduleSync(profileId)
    }

    /**
     * Triggers a sync operation for the profile associated with the specified goal.
     *
     * @param goalId The unique identifier of the goal
     */
    private suspend fun triggerSyncByGoalId(goalId: String) {
        val goal = goalDao.getGoal(goalId) ?: return
        scheduleSync(goal.profileId)
    }

    /**
     * Schedules a sync operation for the user associated with the specified profile.
     *
     * @param profileId The unique identifier of the profile
     */
    private suspend fun scheduleSync(profileId: String) {
        val profile = profileDao.getProfile(profileId) ?: return
        enqueueWorker(profile.userId)
    }

    /**
     * Enqueues a sync worker for the specified user.
     *
     * @param userId The unique identifier of the user to sync
     */
    private fun enqueueWorker(userId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("USER_ID" to userId))
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "sync_user_$userId",
            ExistingWorkPolicy.REPLACE,
            request
        )
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

    override suspend fun upsertTask(task: Task) {
        taskDao.upsert(task.toEntity())
        scheduleSync(task.goalId)
    }

    override suspend fun deleteTask(taskId: String) {
        val task = taskDao.getTask(taskId)
        taskDao.softDelete(taskId)
        task?.let { scheduleSync(it.goalId) }
    }

    override suspend fun updateTaskStatus(taskId: String, status: Boolean) {
        taskDao.updateStatus(taskId, status)
        triggerSyncByTaskId(taskId)
    }

    override suspend fun updateTaskPriority(taskId: String, priority: Int) {
        taskDao.updatePriority(taskId, priority)
        triggerSyncByTaskId(taskId)
    }

    override suspend fun updateTaskOrder(taskId: String, order: Int) {
        taskDao.updateOrder(taskId, order)
        triggerSyncByTaskId(taskId)
    }

    override suspend fun syncTask(goalId: String) {
        scheduleSync(goalId)
    }

    /**
     * Triggers a sync operation for the profile associated with the specified task.
     *
     * @param taskId The unique identifier of the task
     */
    private suspend fun triggerSyncByTaskId(taskId: String) {
        val task = taskDao.getTask(taskId) ?: return
        scheduleSync(task.goalId)
    }

    /**
     * Schedules a sync operation for the user associated with the specified goal.
     *
     * @param goalId The unique identifier of the goal
     */
    private suspend fun scheduleSync(goalId: String) {
        val goal = goalDao.getGoal(goalId) ?: return
        val profile = profileDao.getProfile(goal.profileId) ?: return

        enqueueWorker(profile.userId)
    }

    /**
     * Enqueues a sync worker for the specified user.
     *
     * @param userId The unique identifier of the user to sync
     */
    private fun enqueueWorker(userId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("USER_ID" to userId))
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "sync_user_$userId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}