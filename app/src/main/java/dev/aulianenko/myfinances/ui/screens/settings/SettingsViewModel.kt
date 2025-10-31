package dev.aulianenko.myfinances.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aulianenko.myfinances.data.MockDataGenerator
import dev.aulianenko.myfinances.data.entity.ExchangeRate
import dev.aulianenko.myfinances.data.export.ExportImportRepository
import dev.aulianenko.myfinances.data.repository.ThemeMode
import dev.aulianenko.myfinances.data.repository.UserPreferencesRepository
import dev.aulianenko.myfinances.domain.usecase.CurrencyConversionUseCase
import dev.aulianenko.myfinances.notification.NotificationScheduler
import dev.aulianenko.myfinances.security.BiometricAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val baseCurrency: String = "USD",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fromCurrency: String = "USD",
    val toCurrency: String = "EUR",
    val amount: String = "100",
    val convertedAmount: Double? = null,
    val exchangeRates: List<ExchangeRate> = emptyList(),
    val isLoading: Boolean = true,
    val isGeneratingMockData: Boolean = false,
    val showPortfolioValue: Boolean = true,
    val showPortfolioTrend: Boolean = true,
    val showPortfolioDistribution: Boolean = true,
    val showPortfolioGrowth: Boolean = true,
    val showBestWorstPerformers: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val reminderFrequencyDays: Int = 7,
    val isRefreshingRates: Boolean = false,
    val refreshRatesMessage: String? = null,
    val biometricEnabled: Boolean = false,
    val appLockEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportImportMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val currencyConversionUseCase: CurrencyConversionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mockDataGenerator: MockDataGenerator,
    private val notificationScheduler: NotificationScheduler,
    private val biometricAuthManager: BiometricAuthManager,
    private val exportImportRepository: ExportImportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Load base currency preference
            userPreferencesRepository.baseCurrency.collect { baseCurrency ->
                _uiState.update { it.copy(baseCurrency = baseCurrency) }
            }
        }

        viewModelScope.launch {
            // Load theme mode preference
            userPreferencesRepository.themeMode.collect { themeMode ->
                _uiState.update { it.copy(themeMode = themeMode) }
            }
        }

        viewModelScope.launch {
            // Load exchange rates
            currencyConversionUseCase.getAllExchangeRates().collect { rates ->
                _uiState.update {
                    it.copy(
                        exchangeRates = rates,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            // Load dashboard card visibility preferences
            userPreferencesRepository.showPortfolioValue.collect { show ->
                _uiState.update { it.copy(showPortfolioValue = show) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.showPortfolioTrend.collect { show ->
                _uiState.update { it.copy(showPortfolioTrend = show) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.showPortfolioDistribution.collect { show ->
                _uiState.update { it.copy(showPortfolioDistribution = show) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.showPortfolioGrowth.collect { show ->
                _uiState.update { it.copy(showPortfolioGrowth = show) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.showBestWorstPerformers.collect { show ->
                _uiState.update { it.copy(showBestWorstPerformers = show) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.notificationsEnabled.collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.reminderFrequencyDays.collect { days ->
                _uiState.update { it.copy(reminderFrequencyDays = days) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.biometricEnabled.collect { enabled ->
                _uiState.update { it.copy(biometricEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.appLockEnabled.collect { enabled ->
                _uiState.update { it.copy(appLockEnabled = enabled) }
            }
        }

        // Check if biometric authentication is available
        _uiState.update {
            it.copy(isBiometricAvailable = biometricAuthManager.isBiometricAuthAvailable()
                == dev.aulianenko.myfinances.security.BiometricAvailability.AVAILABLE)
        }
    }

    fun setBaseCurrency(currencyCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setBaseCurrency(currencyCode)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun setFromCurrency(currencyCode: String) {
        _uiState.update { it.copy(fromCurrency = currencyCode) }
        performConversion()
    }

    fun setToCurrency(currencyCode: String) {
        _uiState.update { it.copy(toCurrency = currencyCode) }
        performConversion()
    }

    fun setAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
        performConversion()
    }

    fun swapCurrencies() {
        val currentFrom = _uiState.value.fromCurrency
        val currentTo = _uiState.value.toCurrency
        _uiState.update {
            it.copy(
                fromCurrency = currentTo,
                toCurrency = currentFrom
            )
        }
        performConversion()
    }

    private fun performConversion() {
        viewModelScope.launch {
            val amount = _uiState.value.amount.toDoubleOrNull()
            if (amount != null && amount > 0) {
                val converted = currencyConversionUseCase.convert(
                    amount = amount,
                    fromCurrency = _uiState.value.fromCurrency,
                    toCurrency = _uiState.value.toCurrency
                )
                _uiState.update { it.copy(convertedAmount = converted) }
            } else {
                _uiState.update { it.copy(convertedAmount = null) }
            }
        }
    }

    fun generateMockData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingMockData = true) }
            try {
                mockDataGenerator.generateMockData()
            } finally {
                _uiState.update { it.copy(isGeneratingMockData = false) }
            }
        }
    }

    fun setShowPortfolioValue(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowPortfolioValue(show)
        }
    }

    fun setShowPortfolioTrend(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowPortfolioTrend(show)
        }
    }

    fun setShowPortfolioDistribution(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowPortfolioDistribution(show)
        }
    }

    fun setShowPortfolioGrowth(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowPortfolioGrowth(show)
        }
    }

    fun setShowBestWorstPerformers(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowBestWorstPerformers(show)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
            notificationScheduler.updateReminderSchedule(
                _uiState.value.reminderFrequencyDays,
                enabled
            )
        }
    }

    fun setReminderFrequencyDays(days: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setReminderFrequencyDays(days)
            if (_uiState.value.notificationsEnabled) {
                notificationScheduler.scheduleReminders(days)
            }
        }
    }

    fun refreshExchangeRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingRates = true, refreshRatesMessage = null) }
            try {
                val result = currencyConversionUseCase.updateExchangeRatesFromApi()
                result.fold(
                    onSuccess = { count ->
                        _uiState.update {
                            it.copy(
                                isRefreshingRates = false,
                                refreshRatesMessage = "Successfully updated $count exchange rates"
                            )
                        }
                        // Clear message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        _uiState.update { it.copy(refreshRatesMessage = null) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isRefreshingRates = false,
                                refreshRatesMessage = "Failed to update rates: ${error.message}"
                            )
                        }
                        // Clear error message after 5 seconds
                        kotlinx.coroutines.delay(5000)
                        _uiState.update { it.copy(refreshRatesMessage = null) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRefreshingRates = false,
                        refreshRatesMessage = "Error: ${e.message}"
                    )
                }
                // Clear error message after 5 seconds
                kotlinx.coroutines.delay(5000)
                _uiState.update { it.copy(refreshRatesMessage = null) }
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBiometricEnabled(enabled)
            // If biometric is being enabled, also enable app lock
            if (enabled) {
                userPreferencesRepository.setAppLockEnabled(true)
            }
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAppLockEnabled(enabled)
            // If app lock is being disabled, also disable biometric
            if (!enabled) {
                userPreferencesRepository.setBiometricEnabled(false)
            }
        }
    }

    /**
     * Export all data to a file.
     * @param uri The URI to write the export file to
     */
    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportImportMessage = null) }
            try {
                val result = exportImportRepository.exportToFile(uri)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                exportImportMessage = "Data exported successfully"
                            )
                        }
                        // Clear message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        _uiState.update { it.copy(exportImportMessage = null) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                exportImportMessage = "Export failed: ${error.message}"
                            )
                        }
                        // Clear error message after 5 seconds
                        kotlinx.coroutines.delay(5000)
                        _uiState.update { it.copy(exportImportMessage = null) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportImportMessage = "Export error: ${e.message}"
                    )
                }
                // Clear error message after 5 seconds
                kotlinx.coroutines.delay(5000)
                _uiState.update { it.copy(exportImportMessage = null) }
            }
        }
    }

    /**
     * Import data from a file.
     * @param uri The URI to read the import file from
     * @param replaceExisting If true, existing data will be replaced; if false, data will be merged
     */
    fun importData(uri: Uri, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, exportImportMessage = null) }
            try {
                val result = exportImportRepository.importFromFile(uri, replaceExisting)
                result.fold(
                    onSuccess = { importResult ->
                        val message = buildString {
                            append("Data imported successfully:\n")
                            append("${importResult.accountsImported} accounts, ")
                            append("${importResult.accountValuesImported} values, ")
                            append("${importResult.exchangeRatesImported} exchange rates")
                        }
                        _uiState.update {
                            it.copy(
                                isImporting = false,
                                exportImportMessage = message
                            )
                        }
                        // Clear message after 5 seconds
                        kotlinx.coroutines.delay(5000)
                        _uiState.update { it.copy(exportImportMessage = null) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isImporting = false,
                                exportImportMessage = "Import failed: ${error.message}"
                            )
                        }
                        // Clear error message after 5 seconds
                        kotlinx.coroutines.delay(5000)
                        _uiState.update { it.copy(exportImportMessage = null) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        exportImportMessage = "Import error: ${e.message}"
                    )
                }
                // Clear error message after 5 seconds
                kotlinx.coroutines.delay(5000)
                _uiState.update { it.copy(exportImportMessage = null) }
            }
        }
    }

    /**
     * Export all data to an encrypted file with password protection.
     * @param uri The URI to write the encrypted export file to
     * @param password The password to use for encryption
     */
    fun exportEncrypted(uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportImportMessage = null) }
            try {
                val result = exportImportRepository.exportEncrypted(uri, password)
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                exportImportMessage = "Encrypted backup created successfully"
                            )
                        }
                        // Clear message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        _uiState.update { it.copy(exportImportMessage = null) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                exportImportMessage = "Encrypted export failed: ${error.message}"
                            )
                        }
                        // Clear error message after 5 seconds
                        kotlinx.coroutines.delay(5000)
                        _uiState.update { it.copy(exportImportMessage = null) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportImportMessage = "Export error: ${e.message}"
                    )
                }
                // Clear error message after 5 seconds
                kotlinx.coroutines.delay(5000)
                _uiState.update { it.copy(exportImportMessage = null) }
            }
        }
    }

    /**
     * Import data from an encrypted file with password protection.
     * @param uri The URI to read the encrypted import file from
     * @param password The password to use for decryption
     * @param replaceExisting If true, existing data will be replaced; if false, data will be merged
     */
    fun importEncrypted(uri: Uri, password: String, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, exportImportMessage = null) }
            try {
                val result = exportImportRepository.importEncrypted(uri, password, replaceExisting)
                result.fold(
                    onSuccess = { importResult ->
                        val message = buildString {
                            append("Encrypted backup restored successfully:\n")
                            append("${importResult.accountsImported} accounts, ")
                            append("${importResult.accountValuesImported} values, ")
                            append("${importResult.exchangeRatesImported} exchange rates")
                        }
                        _uiState.update {
                            it.copy(
                                isImporting = false,
                                exportImportMessage = message
                            )
                        }
                        // Clear message after 5 seconds
                        kotlinx.coroutines.delay(5000)
                        _uiState.update { it.copy(exportImportMessage = null) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isImporting = false,
                                exportImportMessage = "Encrypted import failed: ${error.message}"
                            )
                        }
                        // Clear error message after 5 seconds
                        kotlinx.coroutines.delay(5000)
                        _uiState.update { it.copy(exportImportMessage = null) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        exportImportMessage = "Import error: ${e.message}"
                    )
                }
                // Clear error message after 5 seconds
                kotlinx.coroutines.delay(5000)
                _uiState.update { it.copy(exportImportMessage = null) }
            }
        }
    }
}
