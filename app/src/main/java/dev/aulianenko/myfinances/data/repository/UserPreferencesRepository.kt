package dev.aulianenko.myfinances.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromString(value: String): ThemeMode {
            return try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                SYSTEM
            }
        }
    }
}

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val BASE_CURRENCY = stringPreferencesKey("base_currency")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SHOW_PORTFOLIO_VALUE = booleanPreferencesKey("show_portfolio_value")
        val SHOW_PORTFOLIO_TREND = booleanPreferencesKey("show_portfolio_trend")
        val SHOW_PORTFOLIO_DISTRIBUTION = booleanPreferencesKey("show_portfolio_distribution")
        val SHOW_PORTFOLIO_GROWTH = booleanPreferencesKey("show_portfolio_growth")
        val SHOW_BEST_WORST_PERFORMERS = booleanPreferencesKey("show_best_worst_performers")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_FREQUENCY_DAYS = intPreferencesKey("reminder_frequency_days")
    }

    companion object {
        const val DEFAULT_BASE_CURRENCY = "USD"
    }

    val baseCurrency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BASE_CURRENCY] ?: DEFAULT_BASE_CURRENCY
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val themeModeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        ThemeMode.fromString(themeModeString)
    }

    val showPortfolioValue: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_PORTFOLIO_VALUE] ?: true
    }

    val showPortfolioTrend: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_PORTFOLIO_TREND] ?: true
    }

    val showPortfolioDistribution: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_PORTFOLIO_DISTRIBUTION] ?: true
    }

    val showPortfolioGrowth: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_PORTFOLIO_GROWTH] ?: true
    }

    val showBestWorstPerformers: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_BEST_WORST_PERFORMERS] ?: true
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    val reminderFrequencyDays: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDER_FREQUENCY_DAYS] ?: 7
    }

    suspend fun setBaseCurrency(currencyCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BASE_CURRENCY] = currencyCode
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun getBaseCurrency(): String {
        return baseCurrency.first()
    }

    suspend fun getThemeMode(): ThemeMode {
        return themeMode.first()
    }

    suspend fun setShowPortfolioValue(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_PORTFOLIO_VALUE] = show
        }
    }

    suspend fun setShowPortfolioTrend(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_PORTFOLIO_TREND] = show
        }
    }

    suspend fun setShowPortfolioDistribution(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_PORTFOLIO_DISTRIBUTION] = show
        }
    }

    suspend fun setShowPortfolioGrowth(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_PORTFOLIO_GROWTH] = show
        }
    }

    suspend fun setShowBestWorstPerformers(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_BEST_WORST_PERFORMERS] = show
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setReminderFrequencyDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_FREQUENCY_DAYS] = days
        }
    }
}
