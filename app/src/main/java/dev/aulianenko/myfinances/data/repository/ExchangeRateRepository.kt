package dev.aulianenko.myfinances.data.repository

import android.util.Log
import dev.aulianenko.myfinances.data.api.FrankfurterApiService
import dev.aulianenko.myfinances.data.dao.ExchangeRateDao
import dev.aulianenko.myfinances.data.entity.ExchangeRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class ExchangeRateRepository(
    private val exchangeRateDao: ExchangeRateDao,
    private val apiService: FrankfurterApiService
) {
    companion object {
        private const val TAG = "ExchangeRateRepository"
    }
    fun getAllExchangeRates(): Flow<List<ExchangeRate>> = exchangeRateDao.getAllExchangeRates()

    fun getExchangeRate(currencyCode: String): Flow<ExchangeRate?> =
        exchangeRateDao.getExchangeRate(currencyCode)

    suspend fun getExchangeRateSync(currencyCode: String): ExchangeRate? =
        exchangeRateDao.getExchangeRateSync(currencyCode)

    suspend fun insertExchangeRate(exchangeRate: ExchangeRate) =
        exchangeRateDao.insertExchangeRate(exchangeRate)

    suspend fun insertExchangeRates(exchangeRates: List<ExchangeRate>) =
        exchangeRateDao.insertExchangeRates(exchangeRates)

    suspend fun deleteExchangeRate(currencyCode: String) =
        exchangeRateDao.deleteExchangeRate(currencyCode)

    suspend fun deleteAllExchangeRates() = exchangeRateDao.deleteAllExchangeRates()

    suspend fun getExchangeRateCount(): Int = exchangeRateDao.getExchangeRateCount()

    /**
     * Initialize exchange rates with default values if database is empty.
     * Rates are approximate and should be updated from an API in production.
     */
    suspend fun initializeDefaultRates() {
        if (getExchangeRateCount() == 0) {
            val defaultRates = listOf(
                ExchangeRate(currencyCode = "USD", rateToUSD = 1.0),
                ExchangeRate(currencyCode = "EUR", rateToUSD = 1.09),
                ExchangeRate(currencyCode = "GBP", rateToUSD = 1.27),
                ExchangeRate(currencyCode = "JPY", rateToUSD = 0.0067),
                ExchangeRate(currencyCode = "CNY", rateToUSD = 0.14),
                ExchangeRate(currencyCode = "AUD", rateToUSD = 0.65),
                ExchangeRate(currencyCode = "CAD", rateToUSD = 0.72),
                ExchangeRate(currencyCode = "CHF", rateToUSD = 1.13),
                ExchangeRate(currencyCode = "INR", rateToUSD = 0.012),
                ExchangeRate(currencyCode = "BRL", rateToUSD = 0.20),
                ExchangeRate(currencyCode = "RUB", rateToUSD = 0.011),
                ExchangeRate(currencyCode = "KRW", rateToUSD = 0.00075),
                ExchangeRate(currencyCode = "MXN", rateToUSD = 0.050),
                ExchangeRate(currencyCode = "ZAR", rateToUSD = 0.055),
                ExchangeRate(currencyCode = "SGD", rateToUSD = 0.74),
                ExchangeRate(currencyCode = "HKD", rateToUSD = 0.13),
                ExchangeRate(currencyCode = "NOK", rateToUSD = 0.095),
                ExchangeRate(currencyCode = "SEK", rateToUSD = 0.096),
                ExchangeRate(currencyCode = "DKK", rateToUSD = 0.15),
                ExchangeRate(currencyCode = "PLN", rateToUSD = 0.25),
                ExchangeRate(currencyCode = "THB", rateToUSD = 0.029),
                ExchangeRate(currencyCode = "IDR", rateToUSD = 0.000064),
                ExchangeRate(currencyCode = "MYR", rateToUSD = 0.22),
                ExchangeRate(currencyCode = "PHP", rateToUSD = 0.018),
                ExchangeRate(currencyCode = "CZK", rateToUSD = 0.044),
                ExchangeRate(currencyCode = "ILS", rateToUSD = 0.27),
                ExchangeRate(currencyCode = "CLP", rateToUSD = 0.0010),
                ExchangeRate(currencyCode = "NZD", rateToUSD = 0.60),
                ExchangeRate(currencyCode = "TRY", rateToUSD = 0.029),
                ExchangeRate(currencyCode = "AED", rateToUSD = 0.27)
            )
            insertExchangeRates(defaultRates)
        }
    }

    /**
     * Fetch latest exchange rates from Frankfurter API and update database.
     * @return Result with number of rates updated, or error message
     */
    suspend fun updateExchangeRatesFromApi(): Result<Int> {
        return try {
            Log.d(TAG, "Fetching exchange rates from API...")

            // Fetch latest rates from API with USD as base currency
            val response = apiService.getLatestRates(base = "USD")

            Log.d(TAG, "Received ${response.rates.size} exchange rates from API")

            // Convert API response to database entities
            val exchangeRates = mutableListOf<ExchangeRate>()

            // Add USD with rate 1.0
            exchangeRates.add(
                ExchangeRate(
                    currencyCode = "USD",
                    rateToUSD = 1.0,
                    lastUpdated = System.currentTimeMillis()
                )
            )

            // Add all other currencies from API response
            response.rates.forEach { (currencyCode, rate) ->
                exchangeRates.add(
                    ExchangeRate(
                        currencyCode = currencyCode,
                        rateToUSD = 1.0 / rate, // API returns USD to currency, we store currency to USD
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            // Update database with new rates
            insertExchangeRates(exchangeRates)

            Log.d(TAG, "Successfully updated ${exchangeRates.size} exchange rates")
            Result.success(exchangeRates.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update exchange rates from API", e)
            Result.failure(e)
        }
    }

    /**
     * Convert amount from one currency to another using USD as intermediary.
     */
    suspend fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount

        val fromRate = getExchangeRateSync(fromCurrency)?.rateToUSD ?: 1.0
        val toRate = getExchangeRateSync(toCurrency)?.rateToUSD ?: 1.0

        // Convert from source currency to USD, then from USD to target currency
        val amountInUSD = amount * fromRate
        return amountInUSD / toRate
    }
}
