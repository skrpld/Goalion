package com.skrpld.goalion.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skrpld.goalion.data.models.Goal
import com.skrpld.goalion.data.models.Profile
import com.skrpld.goalion.data.models.Task

@Database(
    entities = [Profile::class, Goal::class, Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}