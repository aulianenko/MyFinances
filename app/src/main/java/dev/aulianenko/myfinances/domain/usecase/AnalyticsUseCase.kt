package dev.aulianenko.myfinances.domain.usecase

import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.data.repository.AccountRepository
import dev.aulianenko.myfinances.domain.model.AccountCorrelation
import dev.aulianenko.myfinances.domain.model.AccountPerformance
import dev.aulianenko.myfinances.domain.model.AccountTrend
import dev.aulianenko.myfinances.domain.model.PortfolioAnalytics
import dev.aulianenko.myfinances.domain.model.TimePeriod
import dev.aulianenko.myfinances.domain.model.TrendDataPoint
import dev.aulianenko.myfinances.domain.model.TrendDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class AnalyticsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    /**
     * Get portfolio-wide analytics with performance comparisons.
     */
    fun getPortfolioAnalytics(period: TimePeriod): Flow<PortfolioAnalytics> {
        return repository.getAllAccounts().flatMapLatest { accounts ->
            if (accounts.isEmpty()) {
                flowOf(
                    PortfolioAnalytics(
                        accountPerformances = emptyList(),
                        bestPerformer = null,
                        worstPerformer = null,
                        averageGrowthRate = 0.0,
                        totalPortfolioGrowth = 0.0,
                        period = period
                    )
                )
            } else {
                val performanceFlows = accounts.map { account ->
                    repository.getAccountValuesInPeriod(
                        account.id,
                        period.getStartTimestamp(),
                        period.getEndTimestamp()
                    ).flatMapLatest { values ->
                        flowOf(
                            calculateAccountPerformance(
                                accountId = account.id,
                                accountName = account.name,
                                currency = account.currency,
                                values = values,
                                period = period
                            )
                        )
                    }
                }

                combine(performanceFlows) { performances ->
                    val validPerformances = performances.filter { it.dataPoints >= 2 }
                    val bestPerformer = validPerformances.maxByOrNull { it.growthRate }
                    val worstPerformer = validPerformances.minByOrNull { it.growthRate }
                    val averageGrowth = if (validPerformances.isNotEmpty()) {
                        validPerformances.map { it.growthRate }.average()
                    } else 0.0

                    val totalGrowth = if (validPerformances.isNotEmpty()) {
                        val totalStart = validPerformances.sumOf { it.startValue }
                        val totalEnd = validPerformances.sumOf { it.endValue }
                        if (totalStart > 0) ((totalEnd - totalStart) / totalStart) * 100 else 0.0
                    } else 0.0

                    PortfolioAnalytics(
                        accountPerformances = validPerformances,
                        bestPerformer = bestPerformer,
                        worstPerformer = worstPerformer,
                        averageGrowthRate = averageGrowth,
                        totalPortfolioGrowth = totalGrowth,
                        period = period
                    )
                }
            }
        }
    }

    /**
     * Get historical trend data for an account.
     */
    fun getAccountTrend(accountId: String, period: TimePeriod): Flow<AccountTrend?> {
        return repository.getAccountById(accountId).flatMapLatest { account ->
            if (account == null) {
                flowOf(null)
            } else {
                repository.getAccountValuesInPeriod(
                    accountId,
                    period.getStartTimestamp(),
                    period.getEndTimestamp()
                ).flatMapLatest { values ->
                    if (values.isEmpty()) {
                        flowOf(null)
                    } else {
                        val trendDataPoints = values.map {
                            TrendDataPoint(timestamp = it.timestamp, value = it.value)
                        }

                        // Calculate trend direction and strength
                        val (direction, strength) = calculateTrend(values)

                        flowOf(
                            AccountTrend(
                                accountId = accountId,
                                accountName = account.name,
                                currency = account.currency,
                                dataPoints = trendDataPoints,
                                trendDirection = direction,
                                trendStrength = strength
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Calculate correlation between two accounts.
     */
    fun getAccountsCorrelation(
        account1Id: String,
        account2Id: String,
        period: TimePeriod
    ): Flow<AccountCorrelation?> {
        return combine(
            repository.getAccountById(account1Id),
            repository.getAccountById(account2Id),
            repository.getAccountValuesInPeriod(account1Id, period.getStartTimestamp(), period.getEndTimestamp()),
            repository.getAccountValuesInPeriod(account2Id, period.getStartTimestamp(), period.getEndTimestamp())
        ) { acc1, acc2, values1, values2 ->
            if (acc1 == null || acc2 == null || values1.size < 2 || values2.size < 2) {
                null
            } else {
                val correlation = calculateCorrelation(values1, values2)
                AccountCorrelation(
                    account1Id = account1Id,
                    account1Name = acc1.name,
                    account2Id = account2Id,
                    account2Name = acc2.name,
                    correlationCoefficient = correlation,
                    period = period
                )
            }
        }
    }

    private fun calculateAccountPerformance(
        accountId: String,
        accountName: String,
        currency: String,
        values: List<AccountValue>,
        period: TimePeriod
    ): AccountPerformance {
        if (values.size < 2) {
            return AccountPerformance(
                accountId = accountId,
                accountName = accountName,
                currency = currency,
                growthRate = 0.0,
                volatility = 0.0,
                averageDailyChange = 0.0,
                totalGain = 0.0,
                totalGainPercentage = 0.0,
                startValue = values.firstOrNull()?.value ?: 0.0,
                endValue = values.firstOrNull()?.value ?: 0.0,
                period = period,
                dataPoints = values.size
            )
        }

        val sortedValues = values.sortedBy { it.timestamp }
        val startValue = sortedValues.first().value
        val endValue = sortedValues.last().value

        val totalGain = endValue - startValue
        val totalGainPercentage = if (startValue > 0) (totalGain / startValue) * 100 else 0.0

        // Calculate daily changes
        val dailyChanges = sortedValues.zipWithNext { a, b -> b.value - a.value }
        val averageDailyChange = if (dailyChanges.isNotEmpty()) dailyChanges.average() else 0.0

        // Calculate volatility (standard deviation of daily changes)
        val volatility = if (dailyChanges.isNotEmpty()) {
            val mean = dailyChanges.average()
            val variance = dailyChanges.map { (it - mean).pow(2) }.average()
            sqrt(variance)
        } else 0.0

        // Calculate growth rate
        val daysDiff = ((sortedValues.last().timestamp - sortedValues.first().timestamp) / (1000 * 60 * 60 * 24)).toInt()
        val growthRate = if (daysDiff > 0 && startValue > 0) {
            (totalGainPercentage / daysDiff) * 30 // Annualized to 30-day periods
        } else totalGainPercentage

        return AccountPerformance(
            accountId = accountId,
            accountName = accountName,
            currency = currency,
            growthRate = growthRate,
            volatility = volatility,
            averageDailyChange = averageDailyChange,
            totalGain = totalGain,
            totalGainPercentage = totalGainPercentage,
            startValue = startValue,
            endValue = endValue,
            period = period,
            dataPoints = values.size
        )
    }

    private fun calculateTrend(values: List<AccountValue>): Pair<TrendDirection, Double> {
        if (values.size < 2) return Pair(TrendDirection.STABLE, 0.0)

        val sortedValues = values.sortedBy { it.timestamp }

        // Simple linear regression to determine trend
        val n = sortedValues.size
        val x = (0 until n).map { it.toDouble() }
        val y = sortedValues.map { it.value }

        val meanX = x.average()
        val meanY = y.average()

        val numerator = x.zip(y).sumOf { (xi, yi) -> (xi - meanX) * (yi - meanY) }
        val denominator = x.sumOf { (it - meanX).pow(2) }

        val slope = if (denominator != 0.0) numerator / denominator else 0.0

        // Determine direction and strength
        val direction = when {
            slope > 0.5 -> TrendDirection.UPWARD
            slope < -0.5 -> TrendDirection.DOWNWARD
            else -> TrendDirection.STABLE
        }

        val strength = minOf(abs(slope), 1.0)

        return Pair(direction, strength)
    }

    private fun calculateCorrelation(values1: List<AccountValue>, values2: List<AccountValue>): Double {
        if (values1.size < 2 || values2.size < 2) return 0.0

        // Match values by timestamp (use closest timestamps)
        val sorted1 = values1.sortedBy { it.timestamp }
        val sorted2 = values2.sortedBy { it.timestamp }

        val paired = sorted1.mapNotNull { v1 ->
            val closest = sorted2.minByOrNull { abs(it.timestamp - v1.timestamp) }
            if (closest != null && abs(closest.timestamp - v1.timestamp) < 24 * 60 * 60 * 1000) {
                Pair(v1.value, closest.value)
            } else null
        }

        if (paired.size < 2) return 0.0

        val x = paired.map { it.first }
        val y = paired.map { it.second }

        val meanX = x.average()
        val meanY = y.average()

        val numerator = x.zip(y).sumOf { (xi, yi) -> (xi - meanX) * (yi - meanY) }
        val denominatorX = sqrt(x.sumOf { (it - meanX).pow(2) })
        val denominatorY = sqrt(y.sumOf { (it - meanY).pow(2) })

        return if (denominatorX != 0.0 && denominatorY != 0.0) {
            numerator / (denominatorX * denominatorY)
        } else 0.0
    }
}
