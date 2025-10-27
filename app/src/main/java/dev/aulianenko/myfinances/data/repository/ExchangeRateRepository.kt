package dev.aulianenko.myfinances.data.repository

import dev.aulianenko.myfinances.data.dao.ExchangeRateDao
import dev.aulianenko.myfinances.data.entity.ExchangeRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ExchangeRateRepository(
    private val exchangeRateDao: ExchangeRateDao
) {
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
