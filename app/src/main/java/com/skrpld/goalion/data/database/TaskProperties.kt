package com.skrpld.goalion.data.database

import androidx.room.TypeConverter

enum class TaskStatus {
    TODO, DONE
}

enum class TaskPriority(val value: Int) {
    LOW(2),
    NORMAL(1),
    HIGH(0)
}

class AppTypeConverters {
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus) = status.ordinal
    @TypeConverter
    fun toTaskStatus(value: Int) = TaskStatus.entries[value]

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority) = priority.value
    @TypeConverter
    fun toTaskPriority(value: Int) = TaskPriority.entries.first { it.value == value }
}