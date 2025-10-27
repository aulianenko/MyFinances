package dev.aulianenko.myfinances.ui.screens.accountvalue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.aulianenko.myfinances.domain.CurrencyProvider
import dev.aulianenko.myfinances.ui.components.AppTopBar
import dev.aulianenko.myfinances.ui.components.InputField
import dev.aulianenko.myfinances.ui.components.LoadingIndicator
import dev.aulianenko.myfinances.ui.components.PrimaryButton

@Composable
fun AddAccountValueScreen(
    accountId: String,
    onNavigateBack: () -> Unit,
    onValueSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddAccountValueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onValueSaved()
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
                title = "Update Account Value",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else if (uiState.account == null) {
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
        } else {
            val currency = remember(uiState.account?.currency) {
                CurrencyProvider.getCurrencyByCode(uiState.account?.currency ?: "USD")
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Account: ${uiState.account?.name}",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Enter the current value of your account in ${currency?.name ?: uiState.account?.currency}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                InputField(
                    value = uiState.value,
                    onValueChange = viewModel::onValueChange,
                    label = "Current Value (${currency?.symbol ?: uiState.account?.currency})",
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )

                InputField(
                    value = uiState.note,
                    onValueChange = viewModel::onNoteChange,
                    label = "Note (Optional)",
                    placeholder = "Add a note about this update",
                    imeAction = ImeAction.Done,
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                PrimaryButton(
                    text = "Save Value",
                    onClick = viewModel::saveAccountValue,
                    enabled = uiState.value.isNotBlank()
                )
            }
        }
    }
}
