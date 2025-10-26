package dev.aulianenko.myfinances.domain.usecase

import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.data.repository.AccountRepository
import dev.aulianenko.myfinances.domain.model.AccountStatistics
import dev.aulianenko.myfinances.domain.model.PortfolioStatistics
import dev.aulianenko.myfinances.domain.model.TimePeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull

class CalculateStatisticsUseCase(
    private val repository: AccountRepository
) {
    suspend fun getPortfolioStatistics(period: TimePeriod): Flow<PortfolioStatistics> {
        val accounts = repository.getAllAccounts().firstOrNull() ?: emptyList()

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

        return if (accountStatsFlows.isEmpty()) {
            kotlinx.coroutines.flow.flowOf(
                PortfolioStatistics(
                    totalAccounts = 0,
                    accountStatistics = emptyList(),
                    period = period
                )
            )
        } else {
            combine(accountStatsFlows) { statsArray ->
                PortfolioStatistics(
                    totalAccounts = accounts.size,
                    accountStatistics = statsArray.toList(),
                    period = period
                )
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
}
