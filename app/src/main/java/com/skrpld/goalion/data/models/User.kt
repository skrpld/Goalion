package com.skrpld.goalion.data.models

import androidx.room.PrimaryKey
import java.util.UUID

data class User(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val name: String,
    val email: String,
    val password: String,

    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)