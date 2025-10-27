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

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val BASE_CURRENCY = stringPreferencesKey("base_currency")
    }

    companion object {
        const val DEFAULT_BASE_CURRENCY = "USD"
    }

    val baseCurrency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BASE_CURRENCY] ?: DEFAULT_BASE_CURRENCY
    }

    suspend fun setBaseCurrency(currencyCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BASE_CURRENCY] = currencyCode
        }
    }

    suspend fun getBaseCurrency(): String {
        return baseCurrency.first()
    }
}
