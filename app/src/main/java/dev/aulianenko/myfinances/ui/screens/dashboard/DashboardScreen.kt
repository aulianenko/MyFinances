package dev.aulianenko.myfinances.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.aulianenko.myfinances.domain.CurrencyProvider
import dev.aulianenko.myfinances.domain.model.AccountStatistics
import dev.aulianenko.myfinances.domain.model.TimePeriod
import dev.aulianenko.myfinances.ui.components.AppTopBar
import dev.aulianenko.myfinances.ui.components.EmptyState
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

    Scaffold(
        topBar = {
            AppTopBar(title = "Dashboard")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period Selector
                ExposedDropdownMenuBox(
                    expanded = periodExpanded,
                    onExpandedChange = { periodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedPeriod.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Time Period") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false }
                    ) {
                        TimePeriod.entries.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.displayName) },
                                onClick = {
                                    viewModel.onPeriodChange(period)
                                    periodExpanded = false
                                }
                            )
                        }
                    }
                }

                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Accounts",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${uiState.portfolioStatistics?.totalAccounts ?: 0}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Portfolio Distribution
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
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            PieChart(
                                data = portfolioData,
                                showPercentages = true
                            )
                        }
                    }
                }

                // Account Statistics
                Text(
                    text = "Account Performance",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.portfolioStatistics?.accountStatistics ?: emptyList()) { stats ->
                        AccountStatisticsCard(
                            statistics = stats,
                            onClick = { onNavigateToAccountDetail(stats.accountId) }
                        )
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
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = statistics.accountName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current Value",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${currency?.symbol ?: ""} ${numberFormat.format(statistics.currentValue)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (statistics.valueChange != null && statistics.percentageChange != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Change",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val isPositive = statistics.valueChange >= 0
                        Text(
                            text = "${if (isPositive) "+" else ""}${numberFormat.format(statistics.valueChange)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${if (isPositive) "+" else ""}${numberFormat.format(statistics.percentageChange)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (statistics.valueCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${statistics.valueCount} value updates in this period",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
