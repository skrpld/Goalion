package com.skrpld.goalion.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.skrpld.goalion.data.database.TaskStatus

@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "New Goal",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: Int = 1,
    val orderIndex: Int = 0,
    val profileId: Int,
    val updatedAt: Long = System.currentTimeMillis()
)