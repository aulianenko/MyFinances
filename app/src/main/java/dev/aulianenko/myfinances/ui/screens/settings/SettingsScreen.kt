package dev.aulianenko.myfinances.ui.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.aulianenko.myfinances.data.repository.ThemeMode
import dev.aulianenko.myfinances.domain.CurrencyProvider
import dev.aulianenko.myfinances.ui.components.AppTopBar
import dev.aulianenko.myfinances.ui.components.LoadingIndicator
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Update notification settings based on permission result
        viewModel.setNotificationsEnabled(isGranted)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text(
                        text = "Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Base Currency Selector
                item {
                    var baseCurrencyExpanded by remember { mutableStateOf(false) }
                    val availableCurrencies = remember(uiState.exchangeRates) {
                        uiState.exchangeRates.map { it.currencyCode }.sorted()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Base Currency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Portfolio totals will be displayed in this currency",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            ExposedDropdownMenuBox(
                                expanded = baseCurrencyExpanded,
                                onExpandedChange = { baseCurrencyExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = "${uiState.baseCurrency} - ${CurrencyProvider.getCurrencyByCode(uiState.baseCurrency)?.name ?: ""}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Base Currency") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = baseCurrencyExpanded)
                                    },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = baseCurrencyExpanded,
                                    onDismissRequest = { baseCurrencyExpanded = false }
                                ) {
                                    availableCurrencies.forEach { currencyCode ->
                                        val currency = CurrencyProvider.getCurrencyByCode(currencyCode)
                                        DropdownMenuItem(
                                            text = {
                                                Text("$currencyCode - ${currency?.name ?: ""}")
                                            },
                                            onClick = {
                                                viewModel.setBaseCurrency(currencyCode)
                                                baseCurrencyExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Theme Mode Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Theme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Choose your preferred theme mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ThemeMode.entries.forEach { mode ->
                                    val label = when (mode) {
                                        ThemeMode.LIGHT -> "Light"
                                        ThemeMode.DARK -> "Dark"
                                        ThemeMode.SYSTEM -> "System"
                                    }
                                    FilterChip(
                                        selected = uiState.themeMode == mode,
                                        onClick = { viewModel.setThemeMode(mode) },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (uiState.themeMode == mode) FontWeight.SemiBold else FontWeight.Normal
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
                }

                // Dashboard Customization
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Dashboard Customization",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Show or hide cards on your dashboard",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = uiState.showPortfolioValue,
                                    onClick = { viewModel.setShowPortfolioValue(!uiState.showPortfolioValue) },
                                    label = {
                                        Text(
                                            text = "Total Portfolio Value",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )

                                FilterChip(
                                    selected = uiState.showPortfolioTrend,
                                    onClick = { viewModel.setShowPortfolioTrend(!uiState.showPortfolioTrend) },
                                    label = {
                                        Text(
                                            text = "Portfolio Trend Chart",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )

                                FilterChip(
                                    selected = uiState.showPortfolioDistribution,
                                    onClick = { viewModel.setShowPortfolioDistribution(!uiState.showPortfolioDistribution) },
                                    label = {
                                        Text(
                                            text = "Portfolio Distribution",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )

                                FilterChip(
                                    selected = uiState.showPortfolioGrowth,
                                    onClick = { viewModel.setShowPortfolioGrowth(!uiState.showPortfolioGrowth) },
                                    label = {
                                        Text(
                                            text = "Portfolio Growth",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )

                                FilterChip(
                                    selected = uiState.showBestWorstPerformers,
                                    onClick = { viewModel.setShowBestWorstPerformers(!uiState.showBestWorstPerformers) },
                                    label = {
                                        Text(
                                            text = "Best/Worst Performers",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                // Notifications
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Get reminders to update your portfolio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Enable/Disable notifications
                            FilterChip(
                                selected = uiState.notificationsEnabled,
                                onClick = {
                                    val newValue = !uiState.notificationsEnabled
                                    // For Android 13+, request permission when enabling notifications
                                    if (newValue && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.setNotificationsEnabled(newValue)
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (uiState.notificationsEnabled) "Enabled" else "Disabled",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )

                            if (uiState.notificationsEnabled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Reminder Frequency",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(3, 7, 14, 30).forEach { days ->
                                        FilterChip(
                                            selected = uiState.reminderFrequencyDays == days,
                                            onClick = { viewModel.setReminderFrequencyDays(days) },
                                            label = {
                                                Text(
                                                    text = when (days) {
                                                        3 -> "3 days"
                                                        7 -> "Weekly"
                                                        14 -> "Bi-weekly"
                                                        30 -> "Monthly"
                                                        else -> "$days days"
                                                    },
                                                    style = MaterialTheme.typography.labelMedium
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
                    }
                }

                // Exchange Rates
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Exchange Rates",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Update exchange rates from Frankfurter API",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.refreshExchangeRates() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isRefreshingRates,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isRefreshingRates) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                }
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = if (uiState.isRefreshingRates)
                                        "Refreshing Rates..."
                                    else
                                        "Refresh Exchange Rates",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            uiState.refreshRatesMessage?.let { message ->
                                Spacer(modifier = Modifier.height(12.dp))
                                val isSuccess = message.startsWith("Successfully")
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSuccess)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSuccess)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Last updated: ${
                                    if (uiState.exchangeRates.isNotEmpty()) {
                                        val lastUpdated = uiState.exchangeRates.firstOrNull()?.lastUpdated ?: 0
                                        if (lastUpdated > 0) {
                                            java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                                                .format(java.util.Date(lastUpdated))
                                        } else "Never"
                                    } else "Never"
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Mock Data Generator
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Developer Tools",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Generate realistic test data for different financial scenarios",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.generateMockData() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isGeneratingMockData,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isGeneratingMockData) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                }
                                Text(
                                    text = if (uiState.isGeneratingMockData)
                                        "Generating Mock Data..."
                                    else
                                        "Generate Mock Data",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Creates 6 accounts with 12 months of historical data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Currency Converter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Currency Converter
                item {
                    var fromCurrencyExpanded by remember { mutableStateOf(false) }
                    var toCurrencyExpanded by remember { mutableStateOf(false) }
                    val availableCurrencies = remember(uiState.exchangeRates) {
                        uiState.exchangeRates.map { it.currencyCode }.sorted()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Amount Input
                            OutlinedTextField(
                                value = uiState.amount,
                                onValueChange = { viewModel.setAmount(it) },
                                label = { Text("Amount") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // From Currency
                            ExposedDropdownMenuBox(
                                expanded = fromCurrencyExpanded,
                                onExpandedChange = { fromCurrencyExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = "${uiState.fromCurrency} - ${CurrencyProvider.getCurrencyByCode(uiState.fromCurrency)?.name ?: ""}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("From") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromCurrencyExpanded)
                                    },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = fromCurrencyExpanded,
                                    onDismissRequest = { fromCurrencyExpanded = false }
                                ) {
                                    availableCurrencies.forEach { currencyCode ->
                                        val currency = CurrencyProvider.getCurrencyByCode(currencyCode)
                                        DropdownMenuItem(
                                            text = {
                                                Text("$currencyCode - ${currency?.name ?: ""}")
                                            },
                                            onClick = {
                                                viewModel.setFromCurrency(currencyCode)
                                                fromCurrencyExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Swap Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = { viewModel.swapCurrencies() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Swap currencies",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            // To Currency
                            ExposedDropdownMenuBox(
                                expanded = toCurrencyExpanded,
                                onExpandedChange = { toCurrencyExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = "${uiState.toCurrency} - ${CurrencyProvider.getCurrencyByCode(uiState.toCurrency)?.name ?: ""}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("To") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = toCurrencyExpanded)
                                    },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = toCurrencyExpanded,
                                    onDismissRequest = { toCurrencyExpanded = false }
                                ) {
                                    availableCurrencies.forEach { currencyCode ->
                                        val currency = CurrencyProvider.getCurrencyByCode(currencyCode)
                                        DropdownMenuItem(
                                            text = {
                                                Text("$currencyCode - ${currency?.name ?: ""}")
                                            },
                                            onClick = {
                                                viewModel.setToCurrency(currencyCode)
                                                toCurrencyExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Result
                            if (uiState.convertedAmount != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Converted Amount",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val numberFormat = remember {
                                            NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                                                minimumFractionDigits = 2
                                                maximumFractionDigits = 2
                                            }
                                        }
                                        val toCurrency = CurrencyProvider.getCurrencyByCode(uiState.toCurrency)
                                        Text(
                                            text = "${toCurrency?.symbol ?: ""} ${numberFormat.format(uiState.convertedAmount)}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = uiState.toCurrency,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
