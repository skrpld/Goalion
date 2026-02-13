package com.skrpld.goalion.domain.model

import com.android.identity.util.UUID

/**
 * User entity.
 */
data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Profile entity.
 */
data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val description: String,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Goal with associated tasks.
 */
data class GoalWithTasks(
    val goal: Goal,
    val tasks: List<Task>
)

/**
 * Goal entity.
 */
data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val title: String = "New Goal",
    val description: String = "",
    val status: Boolean = false,
    val priority: Int = 1,
    val order: Int = 0,
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L, // Default: 1 week from now
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Task entity.
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val title: String = "New Task",
    val description: String = "",
    val status: Boolean = false,
    val priority: Int = 1,
    val order: Int = 0,
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L, // Default: 1 week from now
    val updatedAt: Long = System.currentTimeMillis()
)