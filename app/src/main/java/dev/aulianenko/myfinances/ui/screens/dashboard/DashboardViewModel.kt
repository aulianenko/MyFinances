package dev.aulianenko.myfinances.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aulianenko.myfinances.domain.model.PortfolioStatistics
import dev.aulianenko.myfinances.domain.model.TimePeriod
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

data class DashboardUiState(
    val portfolioStatistics: PortfolioStatistics? = null,
    val selectedPeriod: TimePeriod = TimePeriod.THREE_MONTHS,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val calculateStatisticsUseCase: CalculateStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState
                .map { it.selectedPeriod }
                .distinctUntilChanged()
                .flatMapLatest { period ->
                    calculateStatisticsUseCase.getPortfolioStatistics(period)
                }
                .collect { statistics ->
                    _uiState.update {
                        it.copy(
                            portfolioStatistics = statistics,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onPeriodChange(period: TimePeriod) {
        _uiState.update { it.copy(selectedPeriod = period, isLoading = true) }
    }
}
