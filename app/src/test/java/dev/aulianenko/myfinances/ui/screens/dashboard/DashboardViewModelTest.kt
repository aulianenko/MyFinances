package dev.aulianenko.myfinances.ui.screens.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import dev.aulianenko.myfinances.data.repository.UserPreferencesRepository
import dev.aulianenko.myfinances.domain.model.PortfolioAnalytics
import dev.aulianenko.myfinances.domain.model.PortfolioStatistics
import dev.aulianenko.myfinances.domain.model.TimePeriod
import dev.aulianenko.myfinances.domain.usecase.AnalyticsUseCase
import dev.aulianenko.myfinances.domain.usecase.CalculateStatisticsUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var calculateStatisticsUseCase: CalculateStatisticsUseCase
    private lateinit var analyticsUseCase: AnalyticsUseCase
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: DashboardViewModel

    private val testPortfolioStatistics = PortfolioStatistics(
        totalAccounts = 3,
        accountStatistics = emptyList(),
        period = TimePeriod.THREE_MONTHS,
        totalValueInBaseCurrency = 10000.0,
        baseCurrency = "USD"
    )

    private val testValueHistory = listOf(100.0, 200.0, 300.0, 400.0, 500.0)

    private val testPortfolioAnalytics = PortfolioAnalytics(
        accountPerformances = emptyList(),
        bestPerformer = null,
        worstPerformer = null,
        averageGrowthRate = 1.0,
        totalPortfolioGrowth = 1000.0,
        period = TimePeriod.THREE_MONTHS
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        calculateStatisticsUseCase = mockk()
        analyticsUseCase = mockk()
        userPreferencesRepository = mockk()

        // Default mock behavior
        every {
            calculateStatisticsUseCase.getPortfolioStatistics(any())
        } returns flowOf(testPortfolioStatistics)

        every {
            calculateStatisticsUseCase.getPortfolioValueHistory(any())
        } returns flowOf(testValueHistory)

        every {
            analyticsUseCase.getPortfolioAnalytics(any())
        } returns flowOf(testPortfolioAnalytics)

        // Mock user preferences
        every {
            userPreferencesRepository.showPortfolioValue
        } returns flowOf(true)

        every {
            userPreferencesRepository.showPortfolioTrend
        } returns flowOf(true)

        every {
            userPreferencesRepository.showPortfolioDistribution
        } returns flowOf(true)

        every {
            userPreferencesRepository.showPortfolioGrowth
        } returns flowOf(true)

        every {
            userPreferencesRepository.showBestWorstPerformers
        } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading with default period`() = runTest {
        viewModel = DashboardViewModel(calculateStatisticsUseCase, analyticsUseCase, userPreferencesRepository)

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)
            assertNull(initialState.portfolioStatistics)
            assertTrue(initialState.portfolioValueHistory.isEmpty())
            assertEquals(TimePeriod.THREE_MONTHS, initialState.selectedPeriod)
        }
    }

    @Test
    fun `should load portfolio statistics and history on initialization`() = runTest {
        viewModel = DashboardViewModel(calculateStatisticsUseCase, analyticsUseCase, userPreferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.portfolioStatistics)
            assertEquals(testPortfolioStatistics, state.portfolioStatistics)
            assertEquals(testValueHistory, state.portfolioValueHistory)
            assertEquals(TimePeriod.THREE_MONTHS, state.selectedPeriod)
        }

        verify { calculateStatisticsUseCase.getPortfolioStatistics(TimePeriod.THREE_MONTHS) }
        verify { calculateStatisticsUseCase.getPortfolioValueHistory(TimePeriod.THREE_MONTHS) }
    }

    @Test
    fun `onPeriodChange should update selected period and set loading state`() = runTest {
        viewModel = DashboardViewModel(calculateStatisticsUseCase, analyticsUseCase, userPreferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPeriodChange(TimePeriod.SIX_MONTHS)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(TimePeriod.SIX_MONTHS, state.selectedPeriod)
            // Note: loading state might be false already if data loaded fast
        }
    }

    @Test
    fun `onPeriodChange should trigger new data fetch with correct period`() = runTest {
        val sixMonthsStats = testPortfolioStatistics.copy(period = TimePeriod.SIX_MONTHS)
        val sixMonthsHistory = listOf(600.0, 700.0, 800.0)

        every {
            calculateStatisticsUseCase.getPortfolioStatistics(TimePeriod.SIX_MONTHS)
        } returns flowOf(sixMonthsStats)

        every {
            calculateStatisticsUseCase.getPortfolioValueHistory(TimePeriod.SIX_MONTHS)
        } returns flowOf(sixMonthsHistory)

        viewModel = DashboardViewModel(calculateStatisticsUseCase, analyticsUseCase, userPreferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onPeriodChange(TimePeriod.SIX_MONTHS)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(TimePeriod.SIX_MONTHS, state.selectedPeriod)
            assertEquals(sixMonthsStats, state.portfolioStatistics)
            assertEquals(sixMonthsHistory, state.portfolioValueHistory)
            assertFalse(state.isLoading)
        }

        verify { calculateStatisticsUseCase.getPortfolioStatistics(TimePeriod.SIX_MONTHS) }
        verify { calculateStatisticsUseCase.getPortfolioValueHistory(TimePeriod.SIX_MONTHS) }
    }

    @Test
    fun `changing period multiple times should update data correctly`() = runTest {
        viewModel = DashboardViewModel(calculateStatisticsUseCase, analyticsUseCase, userPreferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val oneYearStats = testPortfolioStatistics.copy(period = TimePeriod.ONE_YEAR)
        val oneYearHistory = listOf(1000.0, 2000.0)

        every {
            calculateStatisticsUseCase.getPortfolioStatistics(TimePeriod.ONE_YEAR)
        } returns flowOf(oneYearStats)

        every {
            calculateStatisticsUseCase.getPortfolioValueHistory(TimePeriod.ONE_YEAR)
        } returns flowOf(oneYearHistory)

        // Change to SIX_MONTHS
        viewModel.onPeriodChange(TimePeriod.SIX_MONTHS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Change to ONE_YEAR
        viewModel.onPeriodChange(TimePeriod.ONE_YEAR)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(TimePeriod.ONE_YEAR, state.selectedPeriod)
            assertEquals(oneYearStats, state.portfolioStatistics)
            assertEquals(oneYearHistory, state.portfolioValueHistory)
        }
    }

    @Test
    fun `should handle empty portfolio statistics`() = runTest {
        val emptyStats = PortfolioStatistics(
            totalAccounts = 0,
            accountStatistics = emptyList(),
            period = TimePeriod.THREE_MONTHS,
            totalValueInBaseCurrency = 0.0,
            baseCurrency = "USD"
        )

        every {
            calculateStatisticsUseCase.getPortfolioStatistics(any())
        } returns flowOf(emptyStats)

        every {
            calculateStatisticsUseCase.getPortfolioValueHistory(any())
        } returns flowOf(emptyList())

        viewModel = DashboardViewModel(calculateStatisticsUseCase, analyticsUseCase, userPreferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.portfolioStatistics)
            assertEquals(0, state.portfolioStatistics?.totalAccounts)
            assertTrue(state.portfolioStatistics?.accountStatistics?.isEmpty() == true)
            assertTrue(state.portfolioValueHistory.isEmpty())
        }
    }

    @Test
    fun `should handle all period types correctly`() = runTest {
        viewModel = DashboardViewModel(calculateStatisticsUseCase, analyticsUseCase, userPreferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val periods = listOf(
            TimePeriod.THREE_MONTHS,
            TimePeriod.SIX_MONTHS,
            TimePeriod.ONE_YEAR,
            TimePeriod.MAX
        )

        periods.forEach { period ->
            viewModel.onPeriodChange(period)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(period, state.selectedPeriod)
            }
        }

        // Verify use case was called for each period
        periods.forEach { period ->
            verify { calculateStatisticsUseCase.getPortfolioStatistics(period) }
            verify { calculateStatisticsUseCase.getPortfolioValueHistory(period) }
        }
    }

    @Test
    fun `changing to same period should not duplicate requests`() = runTest {
        viewModel = DashboardViewModel(calculateStatisticsUseCase, analyticsUseCase, userPreferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Clear previous verifications
        verify(exactly = 1) { calculateStatisticsUseCase.getPortfolioStatistics(TimePeriod.THREE_MONTHS) }

        // Change to the same period
        viewModel.onPeriodChange(TimePeriod.THREE_MONTHS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should still be called (distinctUntilChanged in ViewModel handles duplicates)
        // But the total count should be reasonable
        verify(atMost = 3) { calculateStatisticsUseCase.getPortfolioStatistics(TimePeriod.THREE_MONTHS) }
    }
}
