package dev.aulianenko.myfinances.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.aulianenko.myfinances.domain.CurrencyProvider
import dev.aulianenko.myfinances.domain.model.AccountStatistics
import dev.aulianenko.myfinances.domain.model.TimePeriod
import dev.aulianenko.myfinances.ui.components.AppTopBar
import dev.aulianenko.myfinances.ui.components.EmptyState
import dev.aulianenko.myfinances.ui.components.LineChart
import dev.aulianenko.myfinances.ui.components.LoadingIndicator
import dev.aulianenko.myfinances.ui.components.PieChart
import dev.aulianenko.myfinances.ui.components.PieChartData
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAccountDetail: (String) -> Unit,
    onNavigateToBulkUpdate: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var periodExpanded by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Dashboard",
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (uiState.portfolioStatistics?.totalAccounts != 0) {
                FloatingActionButton(onClick = onNavigateToBulkUpdate) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Bulk Update Values"
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else if (uiState.portfolioStatistics?.totalAccounts == 0) {
            EmptyState(
                title = "No Accounts",
                description = "Create an account to start tracking your finances"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Period Selector - Modern chip style
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = "TIME PERIOD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimePeriod.entries.forEach { period ->
                                FilterChip(
                                    selected = uiState.selectedPeriod == period,
                                    onClick = { viewModel.onPeriodChange(period) },
                                    label = {
                                        Text(
                                            text = period.displayName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (uiState.selectedPeriod == period) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Summary Card - Total Portfolio Value
                if (uiState.cardVisibility.showPortfolioValue) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Total Portfolio Value",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            val totalValue = uiState.portfolioStatistics?.totalValueInBaseCurrency ?: 0.0
                            val baseCurrency = CurrencyProvider.getCurrencyByCode(
                                uiState.portfolioStatistics?.baseCurrency ?: "USD"
                            )
                            val numberFormat = remember {
                                NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                                    minimumFractionDigits = 2
                                    maximumFractionDigits = 2
                                }
                            }
                            Text(
                                text = "${baseCurrency?.symbol ?: ""}${numberFormat.format(totalValue)}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${uiState.portfolioStatistics?.totalAccounts ?: 0} accounts • ${baseCurrency?.code ?: "USD"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
                }

                // Portfolio Value Trend Chart
                if (uiState.cardVisibility.showPortfolioTrend && uiState.portfolioValueHistory.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = "Portfolio Trend",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                LineChart(
                                    data = uiState.portfolioValueHistory,
                                    lineColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Portfolio Distribution
                if (uiState.cardVisibility.showPortfolioDistribution) {
                    item {
                        val portfolioData = remember(uiState.portfolioStatistics) {
                        val stats = uiState.portfolioStatistics?.accountStatistics ?: emptyList()
                        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                            minimumFractionDigits = 2
                            maximumFractionDigits = 2
                        }

                        stats.mapIndexed { index, accountStats ->
                            val currency = CurrencyProvider.getCurrencyByCode(accountStats.currency)
                            PieChartData(
                                label = accountStats.accountName,
                                value = accountStats.currentValue,
                                formattedValue = "${currency?.symbol ?: ""} ${numberFormat.format(accountStats.currentValue)}"
                            )
                        }
                    }

                    if (portfolioData.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = "Portfolio Distribution",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                PieChart(
                                    data = portfolioData,
                                    showPercentages = true
                                )
                            }
                        }
                    }
                    }
                }

                // Account Statistics
                item {
                    Text(
                        text = "Account Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                items(uiState.portfolioStatistics?.accountStatistics ?: emptyList()) { stats ->
                    AccountStatisticsCard(
                        statistics = stats,
                        onClick = { onNavigateToAccountDetail(stats.accountId) }
                    )
                }

                // Analytics Section
                if (uiState.portfolioAnalytics != null) {
                    val analytics = uiState.portfolioAnalytics

                    // Portfolio Growth Card
                    if (uiState.cardVisibility.showPortfolioGrowth) {
                        item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = "Portfolio Growth",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                val numberFormat = remember {
                                    NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                                        minimumFractionDigits = 2
                                        maximumFractionDigits = 2
                                    }
                                }
                                val isPositive = analytics!!.totalPortfolioGrowth >= 0
                                Text(
                                    text = "${if (isPositive) "+" else ""}${numberFormat.format(analytics.totalPortfolioGrowth)}%",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Average: ${if (analytics.averageGrowthRate >= 0) "+" else ""}${numberFormat.format(analytics.averageGrowthRate)}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.75f)
                                )
                            }
                        }
                        }
                    }

                    // Best and Worst Performers
                    if (uiState.cardVisibility.showBestWorstPerformers) {
                        item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            analytics!!.bestPerformer?.let { best ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp)
                                    ) {
                                        Text(
                                            text = "Best Performer",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = best.accountName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "+${NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                                                minimumFractionDigits = 2
                                                maximumFractionDigits = 2
                                            }.format(best.totalGainPercentage)}%",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }

                            analytics.worstPerformer?.let { worst ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp)
                                    ) {
                                        Text(
                                            text = "Worst Performer",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = worst.accountName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${if (worst.totalGainPercentage >= 0) "+" else ""}${NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                                                minimumFractionDigits = 2
                                                maximumFractionDigits = 2
                                            }.format(worst.totalGainPercentage)}%",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountStatisticsCard(
    statistics: AccountStatistics,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = remember(statistics.currency) {
        CurrencyProvider.getCurrencyByCode(statistics.currency)
    }
    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = statistics.accountName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (statistics.valueChange != null && statistics.percentageChange != null) {
                    val isPositive = statistics.valueChange >= 0
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPositive)
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                        else
                            MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${if (isPositive) "+" else ""}${numberFormat.format(statistics.percentageChange)}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${currency?.symbol ?: ""}${numberFormat.format(statistics.currentValue)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (statistics.valueCount > 0) {
                    Text(
                        text = "${statistics.valueCount} updates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
