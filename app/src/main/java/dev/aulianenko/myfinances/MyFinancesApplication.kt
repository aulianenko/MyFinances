package dev.aulianenko.myfinances

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.aulianenko.myfinances.data.repository.UserPreferencesRepository
import dev.aulianenko.myfinances.domain.usecase.CurrencyConversionUseCase
import dev.aulianenko.myfinances.notification.NotificationHelper
import dev.aulianenko.myfinances.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyFinancesApplication : Application() {

    @Inject
    lateinit var currencyConversionUseCase: CurrencyConversionUseCase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initializeExchangeRates()
        initializeNotifications()
    }

    private fun initializeExchangeRates() {
        applicationScope.launch {
            currencyConversionUseCase.initializeExchangeRates()
        }
    }

    /**
     * Initialize notification channels and restore saved notification schedule
     */
    private fun initializeNotifications() {
        applicationScope.launch {
            // NotificationHelper is injected, which creates channels in its init block

            // Restore notification schedule based on saved preferences
            val notificationsEnabled = userPreferencesRepository.notificationsEnabled.first()
            val reminderFrequencyDays = userPreferencesRepository.reminderFrequencyDays.first()

            if (notificationsEnabled) {
                notificationScheduler.scheduleReminders(reminderFrequencyDays)
            }
        }
    }
}
