package dev.aulianenko.myfinances.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aulianenko.myfinances.data.MockDataGenerator
import dev.aulianenko.myfinances.data.entity.ExchangeRate
import dev.aulianenko.myfinances.data.repository.ThemeMode
import dev.aulianenko.myfinances.data.repository.UserPreferencesRepository
import dev.aulianenko.myfinances.domain.usecase.CurrencyConversionUseCase
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
    val isGeneratingMockData: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val currencyConversionUseCase: CurrencyConversionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mockDataGenerator: MockDataGenerator
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
}
