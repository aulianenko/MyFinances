package dev.aulianenko.myfinances.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.aulianenko.myfinances.data.repository.AccountRepository
import dev.aulianenko.myfinances.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that sends periodic reminders to update account values
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val accountRepository: AccountRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ReminderWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting reminder worker execution")
            // Get all accounts
            val accounts = accountRepository.getAllAccounts().first()
            Log.d(TAG, "Found ${accounts.size} accounts")

            if (accounts.isEmpty()) {
                Log.d(TAG, "No accounts found, skipping reminder")
                return Result.success()
            }

            // Get threshold from preferences (defaults to 7 days)
            val thresholdDays = userPreferencesRepository.reminderFrequencyDays.first()
            Log.d(TAG, "Using threshold of $thresholdDays days")

            // Get all account values to check which accounts need updates
            val now = System.currentTimeMillis()
            val thresholdMillis = now - TimeUnit.DAYS.toMillis(thresholdDays.toLong())

            var accountsNeedingUpdate = 0
            accounts.forEach { account ->
                val values = accountRepository.getAccountValues(account.id).first()
                val lastUpdate = values.maxByOrNull { it.timestamp }?.timestamp ?: 0

                if (lastUpdate < thresholdMillis) {
                    accountsNeedingUpdate++
                    Log.d(TAG, "Account ${account.name} needs update (last updated ${TimeUnit.MILLISECONDS.toDays(now - lastUpdate)} days ago)")
                }
            }

            Log.d(TAG, "Found $accountsNeedingUpdate accounts needing updates")

            // Show reminder notification
            notificationHelper.showUpdateReminder(accountsNeedingUpdate)
            Log.d(TAG, "Reminder notification sent successfully")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing reminder worker", e)
            Result.failure()
        }
    }
}
