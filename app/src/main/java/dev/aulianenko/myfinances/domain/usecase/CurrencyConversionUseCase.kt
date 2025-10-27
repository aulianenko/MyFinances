package dev.aulianenko.myfinances.domain.usecase

import dev.aulianenko.myfinances.data.entity.ExchangeRate
import dev.aulianenko.myfinances.data.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for currency conversion operations.
 * Handles currency conversions using exchange rates from the repository.
 */
class CurrencyConversionUseCase @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository
) {
    /**
     * Get all available exchange rates as a Flow.
     */
    fun getAllExchangeRates(): Flow<List<ExchangeRate>> =
        exchangeRateRepository.getAllExchangeRates()

    /**
     * Get exchange rate for a specific currency.
     */
    fun getExchangeRate(currencyCode: String): Flow<ExchangeRate?> =
        exchangeRateRepository.getExchangeRate(currencyCode)

    /**
     * Convert amount from one currency to another.
     * @param amount The amount to convert
     * @param fromCurrency Source currency code (e.g., "USD")
     * @param toCurrency Target currency code (e.g., "EUR")
     * @return Converted amount in target currency
     */
    suspend fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        return exchangeRateRepository.convert(amount, fromCurrency, toCurrency)
    }

    /**
     * Convert multiple amounts at once (useful for portfolio totals).
     * @param amounts Map of currency codes to amounts
     * @param toCurrency Target currency code
     * @return Total converted amount in target currency
     */
    suspend fun convertMultiple(amounts: Map<String, Double>, toCurrency: String): Double {
        return amounts.entries.sumOf { (fromCurrency, amount) ->
            convert(amount, fromCurrency, toCurrency)
        }
    }

    /**
     * Initialize default exchange rates if database is empty.
     * Should be called when the app starts.
     */
    suspend fun initializeExchangeRates() {
        exchangeRateRepository.initializeDefaultRates()
    }

    /**
     * Update or add a new exchange rate.
     */
    suspend fun updateExchangeRate(exchangeRate: ExchangeRate) {
        exchangeRateRepository.insertExchangeRate(exchangeRate)
    }

    /**
     * Update multiple exchange rates at once (useful for bulk API updates).
     */
    suspend fun updateExchangeRates(exchangeRates: List<ExchangeRate>) {
        exchangeRateRepository.insertExchangeRates(exchangeRates)
    }

    /**
     * Get the exchange rate count (useful for checking if rates are initialized).
     */
    suspend fun getExchangeRateCount(): Int =
        exchangeRateRepository.getExchangeRateCount()

    /**
     * Check if exchange rates need initialization.
     */
    suspend fun needsInitialization(): Boolean =
        getExchangeRateCount() == 0
}
