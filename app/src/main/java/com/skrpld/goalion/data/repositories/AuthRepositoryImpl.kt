package com.skrpld.goalion.data.repositories

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.skrpld.goalion.data.local.UserDao
import com.skrpld.goalion.data.mappers.toDomain
import com.skrpld.goalion.data.mappers.toEntity
import com.skrpld.goalion.data.mappers.toNetwork
import com.skrpld.goalion.data.remote.AuthRemoteDataSource
import com.skrpld.goalion.data.remote.UserRemoteDataSource
import com.skrpld.goalion.data.workers.SyncWorker
import com.skrpld.goalion.domain.User
import com.skrpld.goalion.domain.repositories.AuthRepository

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

    private fun startSync(userId: String) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("USER_ID" to userId))
            .build()
        workManager.enqueue(request)
    }
}
