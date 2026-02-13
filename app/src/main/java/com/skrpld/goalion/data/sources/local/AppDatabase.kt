package com.skrpld.goalion.data.sources.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, ProfileEntity::class, GoalEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun profileDao(): ProfileDao
    abstract fun goalDao(): GoalDao
    abstract fun taskDao(): TaskDao
}