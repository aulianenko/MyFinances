package dev.aulianenko.myfinances.domain.model

/**
 * Represents performance metrics for an account over a specific period.
 */
data class AccountPerformance(
    val accountId: String,
    val accountName: String,
    val currency: String,
    val growthRate: Double, // Percentage growth
    val volatility: Double, // Standard deviation of returns
    val averageDailyChange: Double,
    val totalGain: Double,
    val totalGainPercentage: Double,
    val startValue: Double,
    val endValue: Double,
    val period: TimePeriod,
    val dataPoints: Int
)

/**
 * Portfolio-wide analytics with account performance comparisons.
 */
data class PortfolioAnalytics(
    val accountPerformances: List<AccountPerformance>,
    val bestPerformer: AccountPerformance?,
    val worstPerformer: AccountPerformance?,
    val averageGrowthRate: Double,
    val totalPortfolioGrowth: Double,
    val period: TimePeriod
)

/**
 * Represents a trend data point for visualization.
 */
data class TrendDataPoint(
    val timestamp: Long,
    val value: Double
)

/**
 * Historical trend data for an account.
 */
data class AccountTrend(
    val accountId: String,
    val accountName: String,
    val currency: String,
    val dataPoints: List<TrendDataPoint>,
    val trendDirection: TrendDirection,
    val trendStrength: Double // 0.0 to 1.0
)

enum class TrendDirection {
    UPWARD,
    DOWNWARD,
    STABLE
}

/**
 * Correlation analysis between two accounts.
 */
data class AccountCorrelation(
    val account1Id: String,
    val account1Name: String,
    val account2Id: String,
    val account2Name: String,
    val correlationCoefficient: Double, // -1.0 to 1.0
    val period: TimePeriod
)
