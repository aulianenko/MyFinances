package dev.aulianenko.myfinances.ui.screens.converter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import dev.aulianenko.myfinances.domain.CurrencyProvider
import dev.aulianenko.myfinances.ui.screens.settings.SettingsViewModel
import dev.aulianenko.myfinances.ui.theme.CardShapes
import dev.aulianenko.myfinances.ui.theme.primaryCardElevation
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Currency Converter") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            var fromCurrencyExpanded by remember { mutableStateOf(false) }
            var toCurrencyExpanded by remember { mutableStateOf(false) }
            val availableCurrencies = remember(uiState.exchangeRates) {
                uiState.exchangeRates.map { it.currencyCode }.sorted()
            }

            // Converter Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShapes.large,
                elevation = primaryCardElevation(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Amount Input
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = { viewModel.setAmount(it) },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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
                }
            }

            // Result Card
            if (uiState.convertedAmount != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShapes.extraLarge,
                    elevation = primaryCardElevation(),
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
                            text = "Converted Amount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val numberFormat = remember {
                            NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                                minimumFractionDigits = 2
                                maximumFractionDigits = 2
                            }
                        }
                        val toCurrency = CurrencyProvider.getCurrencyByCode(uiState.toCurrency)
                        Text(
                            text = "${toCurrency?.symbol ?: ""} ${numberFormat.format(uiState.convertedAmount)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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
