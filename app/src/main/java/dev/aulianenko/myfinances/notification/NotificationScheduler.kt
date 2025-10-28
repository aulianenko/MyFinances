package dev.aulianenko.myfinances.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages scheduling of periodic reminder notifications
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val REMINDER_WORK_NAME = "portfolio_reminder_work"
    }

    /**
     * Schedule periodic reminders based on user preferences
     * @param frequencyDays How often to show reminders (in days)
     */
    fun scheduleReminders(frequencyDays: Int) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            repeatInterval = frequencyDays.toLong(),
            repeatIntervalTimeUnit = TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancel all scheduled reminders
     */
    fun cancelReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    /**
     * Update reminder schedule (cancels old and creates new)
     */
    fun updateReminderSchedule(frequencyDays: Int, enabled: Boolean) {
        if (enabled) {
            scheduleReminders(frequencyDays)
        } else {
            cancelReminders()
        }
    }
}
