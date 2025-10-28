package dev.aulianenko.myfinances.notification

import android.content.Context
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

    override suspend fun doWork(): Result {
        return try {
            // Get all accounts
            val accounts = accountRepository.getAllAccounts().first()

            if (accounts.isEmpty()) {
                // No accounts, no need to send reminder
                return Result.success()
            }

            // Get threshold from preferences (defaults to 7 days)
            val thresholdDays = userPreferencesRepository.reminderFrequencyDays.first()

            // Get all account values to check which accounts need updates
            val now = System.currentTimeMillis()
            val thresholdMillis = now - TimeUnit.DAYS.toMillis(thresholdDays.toLong())

            var accountsNeedingUpdate = 0
            accounts.forEach { account ->
                val values = accountRepository.getAccountValues(account.id).first()
                val lastUpdate = values.maxByOrNull { it.timestamp }?.timestamp ?: 0

                if (lastUpdate < thresholdMillis) {
                    accountsNeedingUpdate++
                }
            }

            // Show reminder notification
            notificationHelper.showUpdateReminder(accountsNeedingUpdate)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
