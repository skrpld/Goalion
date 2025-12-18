package com.skrpld.goalion.data

import com.skrpld.goalion.data.database.AppDao
import com.skrpld.goalion.data.models.Goal
import com.skrpld.goalion.data.models.Task

class GoalRepository(private val dao: AppDao) {
    fun getGoalsWithTasks(profileId: Int) = dao.getGoalsWithTasksList(profileId)

    suspend fun upsertGoal(goal: Goal) = dao.upsertGoal(goal)
    suspend fun upsertTask(task: Task) = dao.upsertTask(task)

    suspend fun deleteGoal(goal: Goal) = dao.deleteGoal(goal)
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)

    suspend fun updateGoalOrder(goalId: Int, newOrder: Int) = dao.updateGoalOrder(goalId, newOrder)
    suspend fun updateTaskOrder(taskId: Int, newOrder: Int) = dao.updateTaskOrder(taskId, newOrder)

    suspend fun getProfile() = dao.getAnyProfile()
}