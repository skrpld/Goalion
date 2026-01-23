package com.skrpld.goalion.data.remote

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class NetworkUser(
    val id: String = "",
    val name: String = "",
    val email: String = "",

    @ServerTimestamp
    val updatedAt: Date? = null,
    val isDeleted: Boolean = false
)

data class NetworkProfile(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",

    @ServerTimestamp
    val updatedAt: Date? = null,
    val isDeleted: Boolean = false
)

data class NetworkGoal(
    val id: String = "",
    val profileId: String = "",
    val title: String = "",
    val description: String = "",
    val status: Boolean = false,
    val priority: Int = 0,
    val order: Int = 0,

    @ServerTimestamp
    val updatedAt: Date? = null,
    val isDeleted: Boolean = false
)

data class NetworkTask(
    val id: String = "",
    val goalId: String = "",
    val title: String = "",
    val description: String = "",
    val status: Boolean = false,
    val priority: Int = 0,
    val order: Int = 0,

    @ServerTimestamp
    val updatedAt: Date? = null,
    val isDeleted: Boolean = false
)