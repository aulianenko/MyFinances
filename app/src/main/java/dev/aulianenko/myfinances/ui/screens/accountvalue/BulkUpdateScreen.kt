package dev.aulianenko.myfinances.ui.screens.accountvalue

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.aulianenko.myfinances.domain.CurrencyProvider
import dev.aulianenko.myfinances.ui.components.AppTopBar
import dev.aulianenko.myfinances.ui.components.EmptyState
import dev.aulianenko.myfinances.ui.components.InputField
import dev.aulianenko.myfinances.ui.components.LoadingIndicator
import dev.aulianenko.myfinances.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkUpdateScreen(
    onNavigateBack: () -> Unit,
    onValuesSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BulkUpdateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onValuesSaved()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Bulk Update Values",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading && uiState.accountInputs.isEmpty()) {
            LoadingIndicator()
        } else if (uiState.accountInputs.isEmpty()) {
            EmptyState(
                title = "No Accounts",
                description = "Create accounts first to update their values"
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Update values for multiple accounts at once. Leave empty to skip.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.accountInputs) { input ->
                        AccountValueInputCard(
                            accountInput = input,
                            onValueChange = { value ->
                                viewModel.onValueChange(input.account.id, value)
                            },
                            errorMessage = uiState.validationErrors[input.account.id]
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Date Picker
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Date",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }
                                    Text(
                                        text = dateFormat.format(java.util.Date(uiState.timestamp)),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select Date",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState(
                                initialSelectedDateMillis = uiState.timestamp
                            )
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                viewModel.onTimestampChange(millis)
                                            }
                                            showDatePicker = false
                                        }
                                    ) {
                                        Text("OK")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) {
                                        Text("Cancel")
                                    }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        InputField(
                            value = uiState.note,
                            onValueChange = viewModel::onNoteChange,
                            label = "Note (Optional)",
                            placeholder = "Add a note for all updates",
                            imeAction = ImeAction.Done,
                            singleLine = false
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        PrimaryButton(
                            text = "Save All Values",
                            onClick = viewModel::saveAllValues,
                            enabled = !uiState.isLoading && uiState.accountInputs.any { it.hasValue }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountValueInputCard(
    accountInput: AccountValueInput,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val currency = remember(accountInput.account.currency) {
        CurrencyProvider.getCurrencyByCode(accountInput.account.currency)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (accountInput.hasValue) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = accountInput.account.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = currency?.name ?: accountInput.account.currency,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            InputField(
                value = accountInput.value,
                onValueChange = onValueChange,
                label = "Current Value (${currency?.symbol ?: accountInput.account.currency})",
                placeholder = "0.00",
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
                isError = errorMessage != null,
                errorMessage = errorMessage
            )
        }
    }
}
