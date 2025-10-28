package dev.aulianenko.myfinances.domain.usecase

import app.cash.turbine.test
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.data.entity.ExchangeRate
import dev.aulianenko.myfinances.data.repository.AccountRepository
import dev.aulianenko.myfinances.data.repository.UserPreferencesRepository
import dev.aulianenko.myfinances.domain.model.TimePeriod
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalculateStatisticsUseCaseTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var currencyConversionUseCase: CurrencyConversionUseCase
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var useCase: CalculateStatisticsUseCase

    private val testExchangeRates = listOf(
        ExchangeRate(id = "1", currencyCode = "USD", rateToUSD = 1.0),
        ExchangeRate(id = "2", currencyCode = "EUR", rateToUSD = 1.09),
        ExchangeRate(id = "3", currencyCode = "GBP", rateToUSD = 1.27)
    )

    @Before
    fun setup() {
        accountRepository = mockk()
        currencyConversionUseCase = mockk()
        userPreferencesRepository = mockk()
        useCase = CalculateStatisticsUseCase(
            accountRepository,
            currencyConversionUseCase,
            userPreferencesRepository
        )

        // Default mocks
        every { currencyConversionUseCase.getAllExchangeRates() } returns flowOf(testExchangeRates)
        every { userPreferencesRepository.baseCurrency } returns flowOf("USD")
    }

    @Test
    fun `getPortfolioStatistics should return empty portfolio when no accounts`() = runTest {
        every { accountRepository.getAllAccounts() } returns flowOf(emptyList())

        useCase.getPortfolioStatistics(TimePeriod.THREE_MONTHS).test {
            val stats = awaitItem()
            assertEquals(0, stats.totalAccounts)
            assertTrue(stats.accountStatistics.isEmpty())
            assertEquals(0.0, stats.totalValueInBaseCurrency, 0.001)
            assertEquals("USD", stats.baseCurrency)
            assertEquals(TimePeriod.THREE_MONTHS, stats.period)
            awaitComplete()
        }
    }

    @Test
    fun `getPortfolioStatistics should calculate statistics for single account`() = runTest {
        val account = Account(
            id = "acc1",
            name = "Test Account",
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val latestValue = AccountValue(
            accountId = "acc1",
            value = 1000.0,
            timestamp = System.currentTimeMillis()
        )

        val valuesInPeriod = listOf(
            AccountValue(
                accountId = "acc1",
                value = 800.0,
                timestamp = System.currentTimeMillis() - 1000000
            ),
            latestValue
        )

        every { accountRepository.getAllAccounts() } returns flowOf(listOf(account))
        every { accountRepository.getLatestAccountValue("acc1") } returns flowOf(latestValue)
        every {
            accountRepository.getAccountValuesInPeriod(
                "acc1",
                any(),
                any()
            )
        } returns flowOf(valuesInPeriod)

        useCase.getPortfolioStatistics(TimePeriod.THREE_MONTHS).test {
            val stats = awaitItem()
            assertEquals(1, stats.totalAccounts)
            assertEquals(1, stats.accountStatistics.size)

            val accountStats = stats.accountStatistics[0]
            assertEquals("acc1", accountStats.accountId)
            assertEquals("Test Account", accountStats.accountName)
            assertEquals("USD", accountStats.currency)
            assertEquals(1000.0, accountStats.currentValue, 0.001)
            assertEquals(800.0, accountStats.firstValue ?: 0.0, 0.001)
            assertEquals(200.0, accountStats.valueChange ?: 0.0, 0.001)
            assertEquals(25.0, accountStats.percentageChange ?: 0.0, 0.1)  // (1000-800)/800 * 100 = 25%
            assertEquals(2, accountStats.valueCount)

            // Total in base currency (USD) should equal current value
            assertEquals(1000.0, stats.totalValueInBaseCurrency, 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `getPortfolioStatistics should handle account with no values`() = runTest {
        val account = Account(
            id = "acc1",
            name = "Empty Account",
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        every { accountRepository.getAllAccounts() } returns flowOf(listOf(account))
        every { accountRepository.getLatestAccountValue("acc1") } returns flowOf(null)
        every {
            accountRepository.getAccountValuesInPeriod(
                "acc1",
                any(),
                any()
            )
        } returns flowOf(emptyList())

        useCase.getPortfolioStatistics(TimePeriod.THREE_MONTHS).test {
            val stats = awaitItem()
            assertEquals(1, stats.totalAccounts)
            assertEquals(1, stats.accountStatistics.size)

            val accountStats = stats.accountStatistics[0]
            assertEquals(0.0, accountStats.currentValue, 0.001)
            assertNull(accountStats.firstValue)
            assertNull(accountStats.valueChange)
            assertNull(accountStats.percentageChange)
            assertEquals(0, accountStats.valueCount)
            awaitComplete()
        }
    }

    @Test
    fun `getPortfolioStatistics should convert multiple currencies to base currency`() = runTest {
        val usdAccount = Account(
            id = "acc1",
            name = "USD Account",
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val eurAccount = Account(
            id = "acc2",
            name = "EUR Account",
            currency = "EUR",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val usdValue = AccountValue(accountId = "acc1", value = 1000.0, timestamp = System.currentTimeMillis())
        val eurValue = AccountValue(accountId = "acc2", value = 500.0, timestamp = System.currentTimeMillis())

        every { accountRepository.getAllAccounts() } returns flowOf(listOf(usdAccount, eurAccount))
        every { accountRepository.getLatestAccountValue("acc1") } returns flowOf(usdValue)
        every { accountRepository.getLatestAccountValue("acc2") } returns flowOf(eurValue)
        every { accountRepository.getAccountValuesInPeriod("acc1", any(), any()) } returns flowOf(listOf(usdValue))
        every { accountRepository.getAccountValuesInPeriod("acc2", any(), any()) } returns flowOf(listOf(eurValue))

        useCase.getPortfolioStatistics(TimePeriod.THREE_MONTHS).test {
            val stats = awaitItem()
            assertEquals(2, stats.totalAccounts)
            assertEquals(2, stats.accountStatistics.size)

            // 1000 USD * 1.0 + 500 EUR * 1.09 = 1000 + 545 = 1545
            assertEquals(1545.0, stats.totalValueInBaseCurrency, 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `getPortfolioValueHistory should return empty list when no accounts`() = runTest {
        every { accountRepository.getAllAccounts() } returns flowOf(emptyList())

        useCase.getPortfolioValueHistory(TimePeriod.THREE_MONTHS).test {
            val history = awaitItem()
            assertTrue(history.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `getPortfolioValueHistory should aggregate values by timestamp`() = runTest {
        val account1 = Account(
            id = "acc1",
            name = "Account 1",
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val account2 = Account(
            id = "acc2",
            name = "Account 2",
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val timestamp1 = System.currentTimeMillis() - 2000000
        val timestamp2 = System.currentTimeMillis() - 1000000

        val allValues = listOf(
            AccountValue(accountId = "acc1", value = 100.0, timestamp = timestamp1),
            AccountValue(accountId = "acc2", value = 200.0, timestamp = timestamp1),
            AccountValue(accountId = "acc1", value = 150.0, timestamp = timestamp2),
            AccountValue(accountId = "acc2", value = 250.0, timestamp = timestamp2)
        )

        every { accountRepository.getAllAccounts() } returns flowOf(listOf(account1, account2))
        every { accountRepository.getAllAccountValuesInPeriod(any(), any()) } returns flowOf(allValues)

        useCase.getPortfolioValueHistory(TimePeriod.THREE_MONTHS).test {
            val history = awaitItem()
            assertEquals(2, history.size)
            // First timestamp: 100 + 200 = 300
            assertEquals(300.0, history[0], 0.001)
            // Second timestamp: 150 + 250 = 400
            assertEquals(400.0, history[1], 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `getPortfolioValueHistory should convert currencies correctly`() = runTest {
        val usdAccount = Account(
            id = "acc1",
            name = "USD Account",
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val eurAccount = Account(
            id = "acc2",
            name = "EUR Account",
            currency = "EUR",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val timestamp = System.currentTimeMillis()

        val allValues = listOf(
            AccountValue(accountId = "acc1", value = 100.0, timestamp = timestamp),
            AccountValue(accountId = "acc2", value = 100.0, timestamp = timestamp)
        )

        every { accountRepository.getAllAccounts() } returns flowOf(listOf(usdAccount, eurAccount))
        every { accountRepository.getAllAccountValuesInPeriod(any(), any()) } returns flowOf(allValues)

        useCase.getPortfolioValueHistory(TimePeriod.THREE_MONTHS).test {
            val history = awaitItem()
            assertEquals(1, history.size)
            // 100 USD * 1.0 + 100 EUR * 1.09 = 100 + 109 = 209
            assertEquals(209.0, history[0], 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `getPortfolioStatistics should use correct base currency from preferences`() = runTest {
        val account = Account(
            id = "acc1",
            name = "Test Account",
            currency = "USD",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val value = AccountValue(accountId = "acc1", value = 1000.0, timestamp = System.currentTimeMillis())

        every { accountRepository.getAllAccounts() } returns flowOf(listOf(account))
        every { accountRepository.getLatestAccountValue("acc1") } returns flowOf(value)
        every { accountRepository.getAccountValuesInPeriod("acc1", any(), any()) } returns flowOf(listOf(value))
        every { userPreferencesRepository.baseCurrency } returns flowOf("EUR")

        useCase.getPortfolioStatistics(TimePeriod.THREE_MONTHS).test {
            val stats = awaitItem()
            assertEquals("EUR", stats.baseCurrency)
            // 1000 USD * 1.0 / 1.09 (EUR rate) ≈ 917.43
            assertEquals(917.43, stats.totalValueInBaseCurrency, 1.0)
            awaitComplete()
        }
    }
}
