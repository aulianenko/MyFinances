package dev.aulianenko.myfinances.data

import dev.aulianenko.myfinances.data.dao.AccountDao
import dev.aulianenko.myfinances.data.dao.AccountValueDao
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.entity.AccountValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import kotlin.random.Random

/**
 * Generates realistic mock data for testing the finance tracking app.
 * Creates accounts with various scenarios: growth, decline, volatility, and stability.
 */
class MockDataGenerator @Inject constructor(
    private val accountDao: AccountDao,
    private val accountValueDao: AccountValueDao
) {

    /**
     * Generates comprehensive mock data including:
     * - 6 diverse accounts across different currencies
     * - Historical data spanning 12 months
     * - Various financial scenarios (growth, decline, volatile, stable)
     */
    suspend fun generateMockData() = withContext(Dispatchers.IO) {
        val accounts = createMockAccounts()

        accounts.forEach { account ->
            accountDao.insertAccount(account)
        }

        // Generate account values for each account
        generateAccountValues(accounts[0], ScenarioType.STEADY_GROWTH, 10000.0, 12)
        generateAccountValues(accounts[1], ScenarioType.VOLATILE_GROWTH, 50000.0, 12)
        generateAccountValues(accounts[2], ScenarioType.DECLINE, 30000.0, 12)
        generateAccountValues(accounts[3], ScenarioType.STABLE, 15000.0, 12)
        generateAccountValues(accounts[4], ScenarioType.RECOVERY, 25000.0, 12)
        generateAccountValues(accounts[5], ScenarioType.EXPONENTIAL_GROWTH, 5000.0, 12)
    }

    private fun createMockAccounts(): List<Account> {
        val now = System.currentTimeMillis()
        val oneYearAgo = now - (365L * 24 * 60 * 60 * 1000)

        return listOf(
            Account(
                name = "Savings Account",
                currency = "USD",
                createdAt = oneYearAgo,
                updatedAt = now
            ),
            Account(
                name = "Stock Portfolio",
                currency = "USD",
                createdAt = oneYearAgo,
                updatedAt = now
            ),
            Account(
                name = "Crypto Wallet",
                currency = "USD",
                createdAt = oneYearAgo,
                updatedAt = now
            ),
            Account(
                name = "Emergency Fund",
                currency = "EUR",
                createdAt = oneYearAgo,
                updatedAt = now
            ),
            Account(
                name = "Investment Account",
                currency = "GBP",
                createdAt = oneYearAgo,
                updatedAt = now
            ),
            Account(
                name = "Retirement Fund",
                currency = "JPY",
                createdAt = oneYearAgo,
                updatedAt = now
            )
        )
    }

    private suspend fun generateAccountValues(
        account: Account,
        scenarioType: ScenarioType,
        startingValue: Double,
        monthsOfData: Int
    ) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.add(Calendar.MONTH, -monthsOfData)

        var currentValue = startingValue
        val values = mutableListOf<AccountValue>()

        // Generate weekly data points
        val weeksOfData = monthsOfData * 4
        for (week in 0..weeksOfData) {
            val timestamp = calendar.timeInMillis

            // Apply scenario-specific growth pattern
            currentValue = applyScenario(
                currentValue = currentValue,
                scenarioType = scenarioType,
                weekNumber = week,
                totalWeeks = weeksOfData,
                startingValue = startingValue
            )

            // Add some realistic randomness
            val randomFactor = 1.0 + (Random.nextDouble(-0.02, 0.02))
            currentValue *= randomFactor

            // Ensure value doesn't go negative
            currentValue = currentValue.coerceAtLeast(startingValue * 0.1)

            val note = when {
                week == 0 -> "Initial balance"
                week == weeksOfData -> "Current balance"
                week % 13 == 0 -> "Quarterly review"
                else -> null
            }

            values.add(
                AccountValue(
                    accountId = account.id,
                    value = currentValue,
                    timestamp = timestamp,
                    note = note
                )
            )

            // Move to next week
            calendar.add(Calendar.DAY_OF_MONTH, 7)
        }

        // Insert all values
        values.forEach { accountValueDao.insertAccountValue(it) }
    }

    private fun applyScenario(
        currentValue: Double,
        scenarioType: ScenarioType,
        weekNumber: Int,
        totalWeeks: Int,
        startingValue: Double
    ): Double {
        return when (scenarioType) {
            ScenarioType.STEADY_GROWTH -> {
                // Consistent 8% annual growth (~0.15% per week)
                currentValue * 1.0015
            }

            ScenarioType.VOLATILE_GROWTH -> {
                // High volatility with overall positive trend
                val baseGrowth = 1.002 // ~10% annual
                val volatility = Random.nextDouble(-0.05, 0.08)
                currentValue * (baseGrowth + volatility)
            }

            ScenarioType.DECLINE -> {
                // Gradual decline (-20% over the period)
                val progress = weekNumber.toDouble() / totalWeeks
                val declineFactor = 1.0 - (0.20 * progress)
                startingValue * declineFactor
            }

            ScenarioType.STABLE -> {
                // Very stable with minimal fluctuations
                val tinyChange = Random.nextDouble(-0.001, 0.001)
                currentValue * (1.0 + tinyChange)
            }

            ScenarioType.RECOVERY -> {
                // Drops first, then recovers
                val progress = weekNumber.toDouble() / totalWeeks
                if (progress < 0.4) {
                    // Drop 30% in first 40%
                    startingValue * (1.0 - (0.30 * progress / 0.4))
                } else {
                    // Recover and grow to 120% of starting value
                    val recoveryProgress = (progress - 0.4) / 0.6
                    val targetValue = startingValue * 1.2
                    val lowPoint = startingValue * 0.7
                    lowPoint + (targetValue - lowPoint) * recoveryProgress
                }
            }

            ScenarioType.EXPONENTIAL_GROWTH -> {
                // Accelerating growth (like successful startup investment)
                val progress = weekNumber.toDouble() / totalWeeks
                val growthMultiplier = 1.0 + (2.0 * progress * progress) // Quadratic growth
                startingValue * growthMultiplier
            }
        }
    }

    enum class ScenarioType {
        STEADY_GROWTH,      // Consistent positive returns
        VOLATILE_GROWTH,    // High ups and downs but overall positive
        DECLINE,            // Losing value over time
        STABLE,             // Minimal changes
        RECOVERY,           // Drop then recovery
        EXPONENTIAL_GROWTH  // Accelerating growth
    }
}
