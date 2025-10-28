package dev.aulianenko.myfinances.domain.usecase

import app.cash.turbine.test
import dev.aulianenko.myfinances.data.entity.ExchangeRate
import dev.aulianenko.myfinances.data.repository.ExchangeRateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyConversionUseCaseTest {

    private lateinit var repository: ExchangeRateRepository
    private lateinit var useCase: CurrencyConversionUseCase

    private val testExchangeRates = listOf(
        ExchangeRate(id = "1", currencyCode = "USD", rateToUSD = 1.0),
        ExchangeRate(id = "2", currencyCode = "EUR", rateToUSD = 1.09),
        ExchangeRate(id = "3", currencyCode = "GBP", rateToUSD = 1.27)
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = CurrencyConversionUseCase(repository)
    }

    @Test
    fun `getAllExchangeRates should return flow from repository`() = runTest {
        every { repository.getAllExchangeRates() } returns flowOf(testExchangeRates)

        useCase.getAllExchangeRates().test {
            val rates = awaitItem()
            assertEquals(3, rates.size)
            assertEquals("USD", rates[0].currencyCode)
            assertEquals("EUR", rates[1].currencyCode)
            assertEquals("GBP", rates[2].currencyCode)
            awaitComplete()
        }
    }

    @Test
    fun `getExchangeRate should return flow for specific currency`() = runTest {
        val eurRate = testExchangeRates[1]
        every { repository.getExchangeRate("EUR") } returns flowOf(eurRate)

        useCase.getExchangeRate("EUR").test {
            val rate = awaitItem()
            assertNotNull(rate)
            assertEquals("EUR", rate?.currencyCode)
            assertEquals(1.09, rate?.rateToUSD ?: 0.0, 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `getExchangeRate should return null for non-existent currency`() = runTest {
        every { repository.getExchangeRate("XYZ") } returns flowOf(null)

        useCase.getExchangeRate("XYZ").test {
            val rate = awaitItem()
            assertNull(rate)
            awaitComplete()
        }
    }

    @Test
    fun `convert should delegate to repository convert method`() = runTest {
        val amount = 100.0
        val fromCurrency = "USD"
        val toCurrency = "EUR"
        val expectedResult = 91.74  // Example conversion result

        coEvery { repository.convert(amount, fromCurrency, toCurrency) } returns expectedResult

        val result = useCase.convert(amount, fromCurrency, toCurrency)

        assertEquals(expectedResult, result, 0.01)
        coVerify { repository.convert(amount, fromCurrency, toCurrency) }
    }

    @Test
    fun `convertMultiple should sum converted amounts correctly`() = runTest {
        val amounts = mapOf(
            "USD" to 100.0,
            "EUR" to 50.0,
            "GBP" to 25.0
        )
        val toCurrency = "USD"

        // Mock individual conversions
        coEvery { repository.convert(100.0, "USD", "USD") } returns 100.0
        coEvery { repository.convert(50.0, "EUR", "USD") } returns 54.5  // 50 * 1.09
        coEvery { repository.convert(25.0, "GBP", "USD") } returns 31.75  // 25 * 1.27

        val result = useCase.convertMultiple(amounts, toCurrency)

        assertEquals(186.25, result, 0.01)
        coVerify { repository.convert(100.0, "USD", "USD") }
        coVerify { repository.convert(50.0, "EUR", "USD") }
        coVerify { repository.convert(25.0, "GBP", "USD") }
    }

    @Test
    fun `convertMultiple should return zero for empty amounts map`() = runTest {
        val result = useCase.convertMultiple(emptyMap(), "USD")
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `initializeExchangeRates should call repository initializeDefaultRates`() = runTest {
        coEvery { repository.initializeDefaultRates() } returns Unit

        useCase.initializeExchangeRates()

        coVerify { repository.initializeDefaultRates() }
    }

    @Test
    fun `updateExchangeRate should call repository insertExchangeRate`() = runTest {
        val newRate = ExchangeRate(id = "4", currencyCode = "JPY", rateToUSD = 0.0067)
        coEvery { repository.insertExchangeRate(newRate) } returns Unit

        useCase.updateExchangeRate(newRate)

        coVerify { repository.insertExchangeRate(newRate) }
    }

    @Test
    fun `updateExchangeRates should call repository insertExchangeRates`() = runTest {
        val newRates = testExchangeRates
        coEvery { repository.insertExchangeRates(newRates) } returns Unit

        useCase.updateExchangeRates(newRates)

        coVerify { repository.insertExchangeRates(newRates) }
    }

    @Test
    fun `getExchangeRateCount should return count from repository`() = runTest {
        coEvery { repository.getExchangeRateCount() } returns 10

        val count = useCase.getExchangeRateCount()

        assertEquals(10, count)
        coVerify { repository.getExchangeRateCount() }
    }

    @Test
    fun `needsInitialization should return true when count is zero`() = runTest {
        coEvery { repository.getExchangeRateCount() } returns 0

        val needsInit = useCase.needsInitialization()

        assertTrue(needsInit)
    }

    @Test
    fun `needsInitialization should return false when count is greater than zero`() = runTest {
        coEvery { repository.getExchangeRateCount() } returns 5

        val needsInit = useCase.needsInitialization()

        assertFalse(needsInit)
    }

    @Test
    fun `convert should handle zero amount correctly`() = runTest {
        coEvery { repository.convert(0.0, "USD", "EUR") } returns 0.0

        val result = useCase.convert(0.0, "USD", "EUR")

        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `convert should handle negative amount correctly`() = runTest {
        coEvery { repository.convert(-100.0, "USD", "EUR") } returns -91.74

        val result = useCase.convert(-100.0, "USD", "EUR")

        assertEquals(-91.74, result, 0.01)
    }
}
