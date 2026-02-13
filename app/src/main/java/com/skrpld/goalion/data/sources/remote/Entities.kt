package com.skrpld.goalion.data.sources.remote

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Network user model.
 */
data class NetworkUser(
    val id: String = "",
    val name: String = "",
    val email: String = "",

    @ServerTimestamp
    val updatedAt: Date? = null,
    val isDeleted: Boolean = false
)

/**
 * Network profile model.
 */
data class NetworkProfile(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",

    @ServerTimestamp
    val updatedAt: Date? = null,
    val isDeleted: Boolean = false
)

/**
 * Network goal model.
 */
data class NetworkGoal(
    val id: String = "",
    val profileId: String = "",
    val title: String = "",
    val description: String = "",
    val status: Boolean = false,
    val priority: Int = 0,
    val order: Int = 0,
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L, // Default: 1 week from now

    @ServerTimestamp
    val updatedAt: Date? = null,
    val isDeleted: Boolean = false
)

/**
 * Network task model.
 */
data class NetworkTask(
    val id: String = "",
    val goalId: String = "",
    val title: String = "",
    val description: String = "",
    val status: Boolean = false,
    val priority: Int = 0,
    val order: Int = 0,
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L, // Default: 1 week from now

    @ServerTimestamp
    val updatedAt: Date? = null,
    val isDeleted: Boolean = false
)