package com.skrpld.goalion.data.mappers

fun com.skrpld.goalion.data.local.GoalWithTasks.toDomain(): com.skrpld.goalion.domain.GoalWithTasks {
    return com.skrpld.goalion.domain.GoalWithTasks(
        goal = this.goal.toDomain(),
        tasks = this.tasks.map { it.toDomain() }
    )
}