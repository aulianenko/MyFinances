package dev.aulianenko.myfinances.domain.usecase

import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.data.repository.AccountRepository
import dev.aulianenko.myfinances.data.repository.UserPreferencesRepository
import dev.aulianenko.myfinances.domain.model.AccountStatistics
import dev.aulianenko.myfinances.domain.model.PortfolioStatistics
import dev.aulianenko.myfinances.domain.model.TimePeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalculateStatisticsUseCase @Inject constructor(
    private val repository: AccountRepository,
    private val currencyConversionUseCase: CurrencyConversionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    fun getPortfolioStatistics(period: TimePeriod): Flow<PortfolioStatistics> {
        return repository.getAllAccounts().flatMapLatest { accounts ->
            userPreferencesRepository.baseCurrency.flatMapLatest { baseCurrency ->
                if (accounts.isEmpty()) {
                    flowOf(
                        PortfolioStatistics(
                            totalAccounts = 0,
                            accountStatistics = emptyList(),
                            period = period,
                            totalValueInBaseCurrency = 0.0,
                            baseCurrency = baseCurrency
                        )
                    )
                } else {
                    val accountStatsFlows = accounts.map { account ->
                        combine(
                            repository.getLatestAccountValue(account.id),
                            repository.getAccountValuesInPeriod(
                                account.id,
                                period.getStartTimestamp(),
                                period.getEndTimestamp()
                            )
                        ) { latestValue, valuesInPeriod ->
                            calculateAccountStatistics(
                                accountId = account.id,
                                accountName = account.name,
                                currency = account.currency,
                                latestValue = latestValue,
                                valuesInPeriod = valuesInPeriod,
                                period = period
                            )
                        }
                    }

                    combine(
                        combine(accountStatsFlows) { it.toList() },
                        currencyConversionUseCase.getAllExchangeRates()
                    ) { statsArray, exchangeRates ->
                        // Calculate total value in base currency
                        val currencyAmounts = statsArray.groupBy { it.currency }
                            .mapValues { (_, stats) -> stats.sumOf { it.currentValue } }

                        // Get exchange rates map
                        val ratesMap = exchangeRates.associate { it.currencyCode to it.rateToUSD }
                        val baseRate = ratesMap[baseCurrency] ?: 1.0

                        // Convert each currency amount to base currency
                        val totalInBaseCurrency = currencyAmounts.entries.sumOf { (currency, amount) ->
                            val currencyRate = ratesMap[currency] ?: 1.0
                            // Convert: amount -> USD -> base currency
                            val amountInUSD = amount * currencyRate
                            amountInUSD / baseRate
                        }

                        PortfolioStatistics(
                            totalAccounts = accounts.size,
                            accountStatistics = statsArray,
                            period = period,
                            totalValueInBaseCurrency = totalInBaseCurrency,
                            baseCurrency = baseCurrency
                        )
                    }
                }
            }
        }
    }

    private fun calculateAccountStatistics(
        accountId: String,
        accountName: String,
        currency: String,
        latestValue: AccountValue?,
        valuesInPeriod: List<AccountValue>,
        period: TimePeriod
    ): AccountStatistics {
        val currentValue = latestValue?.value ?: 0.0
        val firstValue = valuesInPeriod.minByOrNull { it.timestamp }?.value

        val valueChange = if (firstValue != null && firstValue != 0.0) {
            currentValue - firstValue
        } else null

        val percentageChange = if (firstValue != null && firstValue != 0.0) {
            ((currentValue - firstValue) / firstValue) * 100
        } else null

        return AccountStatistics(
            accountId = accountId,
            accountName = accountName,
            currency = currency,
            currentValue = currentValue,
            firstValue = firstValue,
            valueChange = valueChange,
            percentageChange = percentageChange,
            valueCount = valuesInPeriod.size,
            period = period
        )
    }

    fun getPortfolioValueHistory(period: TimePeriod): Flow<List<Double>> {
        return repository.getAllAccounts().flatMapLatest { accounts ->
            if (accounts.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    repository.getAllAccountValuesInPeriod(
                        period.getStartTimestamp(),
                        period.getEndTimestamp()
                    ),
                    currencyConversionUseCase.getAllExchangeRates(),
                    userPreferencesRepository.baseCurrency
                ) { allValues, exchangeRates, baseCurrency ->
                    // Group values by timestamp and account
                    val accountMap = accounts.associateBy { it.id }
                    val ratesMap = exchangeRates.associate { it.currencyCode to it.rateToUSD }
                    val baseRate = ratesMap[baseCurrency] ?: 1.0

                    // Group account values by timestamp
                    val valuesByTimestamp = allValues
                        .groupBy { it.timestamp }
                        .toSortedMap()

                    // For each timestamp, calculate total portfolio value
                    valuesByTimestamp.map { (_, values) ->
                        values.sumOf { accountValue ->
                            val account = accountMap[accountValue.accountId]
                            if (account != null) {
                                val currencyRate = ratesMap[account.currency] ?: 1.0
                                // Convert to base currency
                                val amountInUSD = accountValue.value * currencyRate
                                amountInUSD / baseRate
                            } else {
                                0.0
                            }
                        }
                    }
                }
            }
        }
    }
}
