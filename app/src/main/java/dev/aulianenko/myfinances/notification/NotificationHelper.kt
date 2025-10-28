package dev.aulianenko.myfinances.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.aulianenko.myfinances.MainActivity
import dev.aulianenko.myfinances.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for creating and managing notifications
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID_REMINDERS = "portfolio_reminders"
        const val CHANNEL_ID_MILESTONES = "portfolio_milestones"
        const val CHANNEL_ID_ALERTS = "portfolio_alerts"

        const val NOTIFICATION_ID_REMINDER = 1001
        const val NOTIFICATION_ID_MILESTONE = 1002
        const val NOTIFICATION_ID_ALERT = 1003
    }

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels for different types of notifications
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    "Update Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders to update your account values"
                },
                NotificationChannel(
                    CHANNEL_ID_MILESTONES,
                    "Milestones",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Celebrate your financial milestones"
                },
                NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    "Portfolio Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Important changes to your portfolio"
                }
            )

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    /**
     * Show a reminder notification to update account values
     */
    fun showUpdateReminder(accountsNeedingUpdate: Int = 0) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Time to update your portfolio"
        val text = if (accountsNeedingUpdate > 0) {
            "You have $accountsNeedingUpdate account${if (accountsNeedingUpdate > 1) "s" else ""} to update"
        } else {
            "Keep your financial data up to date"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REMINDER, notification)
    }

    /**
     * Show a milestone celebration notification
     */
    fun showMilestoneNotification(milestone: String, value: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MILESTONES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Milestone Achieved!")
            .setContentText("$milestone: $value")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MILESTONE, notification)
    }

    /**
     * Show a portfolio alert notification
     */
    fun showPortfolioAlert(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ALERT, notification)
    }
}
