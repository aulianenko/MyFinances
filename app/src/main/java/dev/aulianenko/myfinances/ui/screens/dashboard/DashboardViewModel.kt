package dev.aulianenko.myfinances.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aulianenko.myfinances.data.repository.UserPreferencesRepository
import dev.aulianenko.myfinances.domain.model.PortfolioAnalytics
import dev.aulianenko.myfinances.domain.model.PortfolioStatistics
import dev.aulianenko.myfinances.domain.model.TimePeriod
import dev.aulianenko.myfinances.domain.usecase.AnalyticsUseCase
import dev.aulianenko.myfinances.domain.usecase.CalculateStatisticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardCardVisibility(
    val showPortfolioValue: Boolean = true,
    val showPortfolioTrend: Boolean = true,
    val showPortfolioDistribution: Boolean = true,
    val showPortfolioGrowth: Boolean = true,
    val showBestWorstPerformers: Boolean = true
)

data class DashboardUiState(
    val portfolioStatistics: PortfolioStatistics? = null,
    val portfolioValueHistory: List<Double> = emptyList(),
    val portfolioAnalytics: PortfolioAnalytics? = null,
    val selectedPeriod: TimePeriod = TimePeriod.THREE_MONTHS,
    val cardVisibility: DashboardCardVisibility = DashboardCardVisibility(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val calculateStatisticsUseCase: CalculateStatisticsUseCase,
    private val analyticsUseCase: AnalyticsUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState
                .map { it.selectedPeriod }
                .distinctUntilChanged()
                .flatMapLatest { period ->
                    kotlinx.coroutines.flow.combine(
                        calculateStatisticsUseCase.getPortfolioStatistics(period),
                        calculateStatisticsUseCase.getPortfolioValueHistory(period),
                        analyticsUseCase.getPortfolioAnalytics(period),
                        userPreferencesRepository.showPortfolioValue,
                        userPreferencesRepository.showPortfolioTrend,
                        userPreferencesRepository.showPortfolioDistribution,
                        userPreferencesRepository.showPortfolioGrowth,
                        userPreferencesRepository.showBestWorstPerformers
                    ) { statistics, history, analytics, showValue, showTrend, showDistribution, showGrowth, showPerformers ->
                        DashboardData(
                            statistics = statistics,
                            history = history,
                            analytics = analytics,
                            visibility = DashboardCardVisibility(
                                showPortfolioValue = showValue,
                                showPortfolioTrend = showTrend,
                                showPortfolioDistribution = showDistribution,
                                showPortfolioGrowth = showGrowth,
                                showBestWorstPerformers = showPerformers
                            )
                        )
                    }
                }
                .collect { data ->
                    _uiState.update {
                        it.copy(
                            portfolioStatistics = data.statistics,
                            portfolioValueHistory = data.history,
                            portfolioAnalytics = data.analytics,
                            cardVisibility = data.visibility,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private data class DashboardData(
        val statistics: PortfolioStatistics?,
        val history: List<Double>,
        val analytics: PortfolioAnalytics?,
        val visibility: DashboardCardVisibility
    )

    fun onPeriodChange(period: TimePeriod) {
        _uiState.update { it.copy(selectedPeriod = period, isLoading = true) }
    }
}
