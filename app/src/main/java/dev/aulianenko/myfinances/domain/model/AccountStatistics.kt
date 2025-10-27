package dev.aulianenko.myfinances.domain.model

data class AccountStatistics(
    val accountId: String,
    val accountName: String,
    val currency: String,
    val currentValue: Double,
    val firstValue: Double?,
    val valueChange: Double?,
    val percentageChange: Double?,
    val valueCount: Int,
    val period: TimePeriod
)

data class PortfolioStatistics(
    val totalAccounts: Int,
    val accountStatistics: List<AccountStatistics>,
    val period: TimePeriod,
    val totalValueInBaseCurrency: Double = 0.0,
    val baseCurrency: String = "USD",
    val lastUpdated: Long = System.currentTimeMillis()
)
