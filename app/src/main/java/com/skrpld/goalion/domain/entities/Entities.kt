package com.skrpld.goalion.domain.entities

import com.android.identity.util.UUID

data class GoalWithTasks(
    val goal: Goal,
    val tasks: List<Task>
)

/**
 * === Entities ===
 */
data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val description: String,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val title: String = "New Goal",
    val description: String = "",
    val status: Boolean = false,
    val priority: Int = 1,
    val order: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val title: String = "New Task",
    val description: String = "",
    val status: Boolean = false,
    val priority: Int = 1,
    val order: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)