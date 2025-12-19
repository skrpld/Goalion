package com.skrpld.goalion.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.skrpld.goalion.data.database.TaskStatus
import com.skrpld.goalion.data.models.GoalWithTasks

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID = "goal_pin_channel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pinned Goals",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for pinned goals and tasks"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showGoalNotification(goalWithTasks: GoalWithTasks) {
        val goal = goalWithTasks.goal
        val tasks = goalWithTasks.tasks

        // Формируем текст: Список задач с иконками статуса
        val taskListText = tasks.joinToString("\n") { task ->
            val statusIcon = if (task.status == TaskStatus.DONE) "✓" else "○"
            "$statusIcon ${task.title}"
        }.ifEmpty { "No tasks yet" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces) // Стандартная иконка
            .setContentTitle(goal.title)
            .setContentText("${tasks.count { it.status != TaskStatus.DONE }} tasks remaining")
            .setStyle(NotificationCompat.BigTextStyle().bigText(taskListText)) // Расширяемый текст
            .setOngoing(true) // Закрепляет уведомление (нельзя смахнуть)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(goal.id, notification)
    }

    fun dismissNotification(goalId: Int) {
        notificationManager.cancel(goalId)
    }
}