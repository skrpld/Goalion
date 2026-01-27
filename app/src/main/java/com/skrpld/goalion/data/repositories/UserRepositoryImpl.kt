package com.skrpld.goalion.data.repositories

import com.skrpld.goalion.data.local.UserDao
import com.skrpld.goalion.data.mappers.toDomain
import com.skrpld.goalion.data.mappers.toEntity
import com.skrpld.goalion.data.mappers.toNetwork
import com.skrpld.goalion.data.remote.UserRemoteDataSource
import com.skrpld.goalion.domain.User
import com.skrpld.goalion.domain.repositories.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val userRemote: UserRemoteDataSource
) : UserRepository {
    override suspend fun getUser(userId: String): User? {
        return userDao.getUser(userId)?.toDomain()
    }

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
}