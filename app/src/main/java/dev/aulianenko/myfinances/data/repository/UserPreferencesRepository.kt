package dev.aulianenko.myfinances.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
}
