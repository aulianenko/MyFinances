package dev.aulianenko.myfinances.ui.screens.settings

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Scaffold(
        topBar = {
            AppTopBar(title = "Settings")
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
