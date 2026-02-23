package com.skrpld.goalion.data.repositories

import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.skrpld.goalion.data.sources.local.GoalDao
import com.skrpld.goalion.data.sources.local.ProfileDao
import com.skrpld.goalion.data.sources.local.TaskDao
import com.skrpld.goalion.data.sources.local.UserDao
import com.skrpld.goalion.data.mappers.toDomain
import com.skrpld.goalion.data.mappers.toEntity
import com.skrpld.goalion.data.mappers.toNetwork
import com.skrpld.goalion.data.sources.remote.AuthRemoteDataSource
import com.skrpld.goalion.data.sources.remote.UserRemoteDataSource
import com.skrpld.goalion.data.util.SyncUtil
import com.skrpld.goalion.data.workers.SyncWorker
import com.skrpld.goalion.domain.model.GoalWithTasks
import com.skrpld.goalion.domain.model.Goal
import com.skrpld.goalion.domain.model.Profile
import com.skrpld.goalion.domain.model.Task
import com.skrpld.goalion.domain.model.User
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

    private val TAG = "GoalionLog_AuthRepo"

    override fun getCurrentUser(): String? = authRemote.getCurrentUserId()

    override suspend fun signUp(name: String, email: String, pass: String): Result<User> {
        Log.d(TAG, "[UPLOAD] Signing up in remote auth: $email")
        return try {
            val firebaseUser = authRemote.signUp(email, pass)
                ?: return Result.failure(Exception("Sign up failed"))

            val user = User(id = firebaseUser.uid, name = name, email = email)

            Log.d(TAG, "[UPLOAD] Pushing new user to Remote DB: ${user.id}")
            userRemote.upsertUser(user.toNetwork())

            Log.d(TAG, "[LOCAL_DB] Saving new user to Local DB: ${user.id}")
            userDao.upsert(user.toEntity())

            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "SignUp failed in Repo", e)
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, pass: String): Result<User> {
        Log.d(TAG, "[DOWNLOAD] Signing in via remote auth: $email")
        return try {
            val firebaseUser = authRemote.signIn(email, pass)
                ?: return Result.failure(Exception("Sign in failed"))

            Log.d(TAG, "[DOWNLOAD] Fetching user profile from Remote DB: ${firebaseUser.uid}")
            val networkUser = userRemote.getUser(firebaseUser.uid)
            val domainUser = networkUser?.toDomain()
                ?: User(id = firebaseUser.uid, name = "Unknown", email = email)

            Log.d(TAG, "[LOCAL_DB] Saving fetched user to Local DB: ${domainUser.id}")
            userDao.upsert(domainUser.toEntity())

            Log.d(TAG, "[SYNC] User signed in. Triggering full SyncWorker for User ID: ${domainUser.id}")
            startSync(domainUser.id)

            Result.success(domainUser)
        } catch (e: Exception) {
            Log.e(TAG, "SignIn failed in Repo", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        Log.d(TAG, "Logging out from remote auth")
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
                Log.d(TAG, "[UPLOAD] Email changed, updating in Auth Remote")
                authRemote.updateEmail(newEmail)
            }

            Log.d(TAG, "[UPLOAD] Upserting user to Remote DB: ${user.id}")
            userRemote.upsertUser(user.toNetwork())

            Log.d(TAG, "[LOCAL_DB] Upserting user to Local DB: ${user.id}")
            userDao.upsert(user.toEntity())

        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Log.w(TAG, "Recent login required to upsert user")
            throw e
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.w(TAG, "Email collision during upsert")
            throw Exception("This Email is already in use")
        } catch (e: Exception) {
            Log.e(TAG, "UpsertUser failed", e)
            throw e
        }
    }

    override suspend fun reLoginAndRetry(email: String, pass: String, userToSave: User) {
        Log.d(TAG, "Re-authenticating to retry operation")
        authRemote.reauthenticate(email, pass)
        upsertUser(userToSave)
    }

    override suspend fun changePassword(currentPass: String, newPass: String): Result<Unit> {
        return try {
            val user = authRemote.getCurrentUser()
                ?: return Result.failure(Exception("User not found"))
            val email = user.email
                ?: return Result.failure(Exception("User email not found"))

            Log.d(TAG, "[UPLOAD] Re-authenticating and updating password")
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
        Log.d(TAG, "[SYNC] Enqueuing SyncWorker for USER_ID: $userId")
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

    private val TAG = "GoalionLog_UserRepo"

    override suspend fun getUser(userId: String): User? {
        Log.d(TAG, "[LOCAL_DB] Fetching user locally: $userId")
        return userDao.getUser(userId)?.toDomain()
    }

    override suspend fun upsert(user: User) {
        Log.d(TAG, "[LOCAL_DB] Upserting user locally: ${user.id}")
        userDao.upsert(user.toEntity())
        try {
            Log.d(TAG, "[UPLOAD] Upserting user to Remote DB: ${user.id}")
            userRemote.upsertUser(user.toNetwork())
        } catch (e: Exception) {
            Log.w(TAG, "[UPLOAD] Failed to upload user, requires delayed sending (TODO)", e)
            TODO("delayed sending")
        }
    }

    override suspend fun delete(userId: String) {
        Log.w(TAG, "[LOCAL_DB] & [UPLOAD] Deleting user locally and remotely: $userId")
        userDao.delete(userId)
        userRemote.deleteUser(userId)
    }

    override suspend fun isNameTaken(name: String): Boolean {
        Log.d(TAG, "[DOWNLOAD] Checking if name is taken in Remote: $name")
        return userRemote.isNameTaken(name)
    }

    override suspend fun isEmailTaken(email: String): Boolean {
        Log.d(TAG, "[DOWNLOAD] Checking if email is taken in Remote: $email")
        return userRemote.isEmailTaken(email)
    }

    override suspend fun fetchUserFromRemote(id: String): User? {
        Log.d(TAG, "[DOWNLOAD] Fetching User from remote explicitly: $id")
        return try {
            val networkUser = userRemote.getUser(id)
            networkUser?.toDomain()?.also { domainUser ->
                Log.d(TAG, "[LOCAL_DB] Saving fetched remote user locally: $id")
                userDao.upsert(domainUser.toEntity())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user from remote", e)
            null
        }
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

    private val TAG = "GoalionLog_ProfileRepo"

    override suspend fun getProfilesByUser(userId: String): List<Profile> {
        Log.d(TAG, "[LOCAL_DB] Fetching Profiles for user: $userId")
        return profileDao.getProfilesByUser(userId).map { it.toDomain() }
    }

    override suspend fun upsert(profile: Profile) {
        Log.d(TAG, "[LOCAL_DB] Upserting Profile: ${profile.id}")
        profileDao.upsert(profile.toEntity())
        Log.d(TAG, "[SYNC] Profile upserted, triggering sync for user: ${profile.userId}")
        startSync(profile.userId)
    }

    override suspend fun delete(profileId: String) {
        Log.w(TAG, "[LOCAL_DB] Soft deleting Profile: $profileId")
        profileDao.softDelete(profileId)
        // Note: Sync logic for delete would normally go here if handled via worker
    }

    override suspend fun sync(userId: String) {
        Log.d(TAG, "[SYNC] Manual sync requested in ProfileRepo for user: $userId")
        startSync(userId)
    }

    /**
     * Starts a sync operation for the specified user.
     *
     * @param userId The unique identifier of the user to sync
     */
    private fun startSync(userId: String) {
        Log.d(TAG, "[SYNC] Enqueuing SyncWorker from ProfileRepo for USER_ID: $userId")
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

    private val TAG = "GoalionLog_GoalRepo"
    private val syncUtil = SyncUtil(goalDao, profileDao, workManager)

    override fun getGoalsWithTasks(profileId: String): Flow<List<GoalWithTasks>> {
        Log.d(TAG, "[LOCAL_DB] Returning Flow for GoalsWithTasks in Profile: $profileId")
        return goalDao.getGoalsWithTasksList(profileId)
            .map { list -> list.toDomain() }
    }

    override suspend fun upsert(goal: Goal) {
        Log.d(TAG, "[LOCAL_DB] Upserting Goal: ${goal.id}")
        goalDao.upsert(goal.toEntity())
        Log.d(TAG, "[SYNC] Goal upserted. Scheduling SyncWorker via ProfileID: ${goal.profileId}")
        syncUtil.scheduleSyncByProfileId(goal.profileId)
    }

    override suspend fun delete(goalId: String) {
        Log.w(TAG, "[LOCAL_DB] Soft deleting Goal: $goalId")
        val goal = goalDao.getGoal(goalId)
        goalDao.softDelete(goalId)
        goal?.let {
            Log.d(TAG, "[SYNC] Goal deleted. Scheduling SyncWorker via ProfileID: ${it.profileId}")
            syncUtil.scheduleSyncByProfileId(it.profileId)
        }
    }

    override suspend fun updateStatus(goalId: String, status: Boolean) {
        Log.d(TAG, "[LOCAL_DB] Updating Status Goal ID: $goalId")
        goalDao.updateStatus(goalId, status)
        Log.d(TAG, "[SYNC] Triggering sync by Goal ID: $goalId")
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updatePriority(goalId: String, priority: Int) {
        Log.d(TAG, "[LOCAL_DB] Updating Priority Goal ID: $goalId")
        goalDao.updatePriority(goalId, priority)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updateOrder(goalId: String, order: Int) {
        Log.d(TAG, "[LOCAL_DB] Updating Order Goal ID: $goalId")
        goalDao.updateOrder(goalId, order)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updateStartDate(goalId: String, startDate: Long) {
        Log.d(TAG, "[LOCAL_DB] Updating StartDate Goal ID: $goalId")
        goalDao.updateStartDate(goalId, startDate)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updateTitle(goalId: String, title: String) {
        Log.d(TAG, "[LOCAL_DB] Updating Title Goal ID: $goalId")
        goalDao.updateTitle(goalId, title)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updateDescription(goalId: String, description: String) {
        Log.d(TAG, "[LOCAL_DB] Updating Description Goal ID: $goalId")
        goalDao.updateDescription(goalId, description)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun updateTargetDate(goalId: String, targetDate: Long) {
        Log.d(TAG, "[LOCAL_DB] Updating TargetDate Goal ID: $goalId")
        goalDao.updateTargetDate(goalId, targetDate)
        syncUtil.triggerSyncByGoalId(goalId)
    }

    override suspend fun sync(profileId: String) {
        Log.d(TAG, "[SYNC] Manual sync requested in GoalRepo for ProfileID: $profileId")
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

    private val TAG = "GoalionLog_TaskRepo"
    private val syncUtil = SyncUtil(goalDao, profileDao, workManager)

    override suspend fun upsert(task: Task) {
        Log.d(TAG, "[LOCAL_DB] Upserting Task: ${task.id}")
        taskDao.upsert(task.toEntity())
        Log.d(TAG, "[SYNC] Task upserted. Scheduling SyncWorker via GoalID: ${task.goalId}")
        syncUtil.scheduleSync(task.goalId)
    }

    override suspend fun delete(taskId: String) {
        Log.w(TAG, "[LOCAL_DB] Soft deleting Task: $taskId")
        val task = taskDao.getTask(taskId)
        taskDao.softDelete(taskId)
        task?.let {
            Log.d(TAG, "[SYNC] Task deleted. Scheduling SyncWorker via GoalID: ${it.goalId}")
            syncUtil.scheduleSync(it.goalId)
        }
    }

    override suspend fun updateStatus(taskId: String, status: Boolean) {
        Log.d(TAG, "[LOCAL_DB] Updating Status Task ID: $taskId")
        taskDao.updateStatus(taskId, status)
        Log.d(TAG, "[SYNC] Triggering sync by Task ID: $taskId")
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updatePriority(taskId: String, priority: Int) {
        Log.d(TAG, "[LOCAL_DB] Updating Priority Task ID: $taskId")
        taskDao.updatePriority(taskId, priority)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updateOrder(taskId: String, order: Int) {
        Log.d(TAG, "[LOCAL_DB] Updating Order Task ID: $taskId")
        taskDao.updateOrder(taskId, order)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updateStartDate(taskId: String, startDate: Long) {
        Log.d(TAG, "[LOCAL_DB] Updating StartDate Task ID: $taskId")
        taskDao.updateStartDate(taskId, startDate)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updateTitle(taskId: String, title: String) {
        Log.d(TAG, "[LOCAL_DB] Updating Title Task ID: $taskId")
        taskDao.updateTitle(taskId, title)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updateDescription(taskId: String, description: String) {
        Log.d(TAG, "[LOCAL_DB] Updating Description Task ID: $taskId")
        taskDao.updateDescription(taskId, description)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun updateTargetDate(taskId: String, targetDate: Long) {
        Log.d(TAG, "[LOCAL_DB] Updating TargetDate Task ID: $taskId")
        taskDao.updateTargetDate(taskId, targetDate)
        syncUtil.triggerSyncByTaskId(taskId, taskDao)
    }

    override suspend fun sync(goalId: String) {
        Log.d(TAG, "[SYNC] Manual sync requested in TaskRepo for GoalID: $goalId")
        syncUtil.scheduleSync(goalId)
    }
}