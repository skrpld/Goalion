package com.skrpld.goalion.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class GoalWithTasks(
    @Embedded val goal: Goal,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val tasks: List<Task>
)