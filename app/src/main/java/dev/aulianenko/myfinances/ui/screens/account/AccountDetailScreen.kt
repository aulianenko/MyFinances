package dev.aulianenko.myfinances.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.domain.CurrencyProvider
import dev.aulianenko.myfinances.ui.components.AppTopBar
import dev.aulianenko.myfinances.ui.components.EmptyState
import dev.aulianenko.myfinances.ui.components.LineChart
import dev.aulianenko.myfinances.ui.components.LoadingIndicator
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun AccountDetailScreen(
    accountId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddValue: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = uiState.account?.name ?: "Account Details",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddValue(accountId) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Value"
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }

            uiState.account == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Account not found",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                val currency = remember(uiState.account?.currency) {
                    CurrencyProvider.getCurrencyByCode(uiState.account?.currency ?: "USD")
                }
                val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                } }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Current Value Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Current Value",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.latestValue != null) {
                                    "${currency?.symbol ?: ""} ${numberFormat.format(uiState.latestValue!!.value)}"
                                } else {
                                    "No values recorded"
                                },
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (uiState.latestValue != null) {
                                Text(
                                    text = currency?.name ?: uiState.account?.currency ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Value Trend Chart
                    if (uiState.accountValues.isNotEmpty()) {
                        var selectedPeriod by remember { mutableStateOf(TimePeriod.ALL) }

                        val filteredValues = remember(uiState.accountValues, selectedPeriod) {
                            val currentTime = System.currentTimeMillis()
                            val cutoffTime = when (selectedPeriod) {
                                TimePeriod.SEVEN_DAYS -> currentTime - TimeUnit.DAYS.toMillis(7)
                                TimePeriod.THIRTY_DAYS -> currentTime - TimeUnit.DAYS.toMillis(30)
                                TimePeriod.NINETY_DAYS -> currentTime - TimeUnit.DAYS.toMillis(90)
                                TimePeriod.ALL -> 0L
                            }
                            uiState.accountValues
                                .filter { it.timestamp >= cutoffTime }
                                .sortedBy { it.timestamp }
                        }

                        val chartData = remember(filteredValues) {
                            filteredValues.map { it.value }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Value Trend",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Time period filter chips
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(TimePeriod.values()) { period ->
                                        FilterChip(
                                            selected = selectedPeriod == period,
                                            onClick = { selectedPeriod = period },
                                            label = { Text(period.label) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (chartData.isNotEmpty()) {
                                    LineChart(
                                        data = chartData,
                                        lineColor = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "No data for selected period",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Value History
                    if (uiState.accountValues.isEmpty()) {
                        EmptyState(
                            title = "No Value History",
                            description = "Add your first value update to start tracking"
                        )
                    } else {
                        Text(
                            text = "Value History",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.accountValues) { accountValue ->
                                AccountValueItem(
                                    accountValue = accountValue,
                                    currencySymbol = currency?.symbol ?: "",
                                    onDelete = { viewModel.deleteAccountValue(accountValue) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountValueItem(
    accountValue: AccountValue,
    currencySymbol: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    } }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$currencySymbol ${numberFormat.format(accountValue.value)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dateFormat.format(Date(accountValue.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!accountValue.note.isNullOrBlank()) {
                    Text(
                        text = accountValue.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Value",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

enum class TimePeriod(val label: String) {
    SEVEN_DAYS("7 Days"),
    THIRTY_DAYS("30 Days"),
    NINETY_DAYS("90 Days"),
    ALL("All")
}
