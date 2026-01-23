package com.skrpld.goalion.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

/**
 * === Data transfer object classes ===
 */
data class GoalWithTasks(
    @Embedded
    val goal: GoalEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val tasks: List<TaskEntity>
)

/**
 * === Models classes ===
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val name: String,
    val email: String,
    val password: String,

    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class ProfileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,

    val title: String,
    val description: String,

    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class GoalEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,

    val title: String = "New Goal",
    val description: String = "",
    val status: Boolean = false,
    val priority: Int = 1,
    val order: Int = 0,

    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goalId")]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,

    val title: String = "New Task",
    val description: String = "",
    val status: Boolean = false,
    val priority: Int = 1,
    val order: Int = 0,

    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)