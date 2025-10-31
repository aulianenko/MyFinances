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
import android.net.Uri
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.aulianenko.myfinances.R
import dev.aulianenko.myfinances.data.repository.ThemeMode
import dev.aulianenko.myfinances.domain.CurrencyProvider
import dev.aulianenko.myfinances.ui.components.AppTopBar
import dev.aulianenko.myfinances.ui.components.LoadingIndicator
import dev.aulianenko.myfinances.ui.theme.CardShapes
import dev.aulianenko.myfinances.ui.theme.secondaryCardElevation
import dev.aulianenko.myfinances.ui.utils.rememberHapticFeedback
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToCurrencyConverter: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
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
                title = stringResource(R.string.settings),
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
                        text = stringResource(R.string.preferences),
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
                        shape = CardShapes.large,
                        elevation = secondaryCardElevation(),
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
                                text = stringResource(R.string.base_currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.base_currency_description),
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
                                    label = { Text(stringResource(R.string.select_base_currency)) },
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
                        shape = CardShapes.large,
                        elevation = secondaryCardElevation(),
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
                                text = stringResource(R.string.theme),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.theme_description),
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
                                        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                        ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
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

                // Security
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShapes.large,
                        elevation = secondaryCardElevation(),
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
                                text = stringResource(R.string.security),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.security_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // App Lock toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.app_lock),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = stringResource(R.string.app_lock_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.appLockEnabled,
                                    onCheckedChange = {
                                        haptic.light()
                                        viewModel.setAppLockEnabled(it)
                                    }
                                )
                            }

                            if (uiState.isBiometricAvailable) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))

                                // Biometric authentication toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.biometric_authentication),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = stringResource(R.string.biometric_description),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = uiState.biometricEnabled,
                                        onCheckedChange = {
                                            haptic.light()
                                            viewModel.setBiometricEnabled(it)
                                        },
                                        enabled = uiState.appLockEnabled
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.biometric_not_available),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Export/Import
                item {
                    // State for password dialogs
                    var showExportPasswordDialog by remember { mutableStateOf(false) }
                    var showImportPasswordDialog by remember { mutableStateOf(false) }
                    var pendingExportUri: Uri? by remember { mutableStateOf(null) }
                    var pendingImportUri: Uri? by remember { mutableStateOf(null) }

                    // Export file launcher
                    val exportLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument("application/json")
                    ) { uri ->
                        uri?.let { viewModel.exportData(it) }
                    }

                    // Import file launcher
                    val importLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        uri?.let { viewModel.importData(it, replaceExisting = false) }
                    }

                    // Encrypted export file launcher
                    val exportEncryptedLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument("application/json")
                    ) { uri ->
                        uri?.let {
                            pendingExportUri = it
                            showExportPasswordDialog = true
                        }
                    }

                    // Encrypted import file launcher
                    val importEncryptedLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        uri?.let {
                            pendingImportUri = it
                            showImportPasswordDialog = true
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShapes.large,
                        elevation = secondaryCardElevation(),
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
                                text = stringResource(R.string.data_management),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.data_management_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Export button
                            Button(
                                onClick = {
                                    val fileName = "myfinances_export_${System.currentTimeMillis()}.json"
                                    exportLauncher.launch(fileName)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isExporting && !uiState.isImporting
                            ) {
                                if (uiState.isExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                }
                                Text(if (uiState.isExporting) stringResource(R.string.exporting) else stringResource(R.string.export_data))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Import button
                            Button(
                                onClick = {
                                    importLauncher.launch(arrayOf("application/json"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isExporting && !uiState.isImporting
                            ) {
                                if (uiState.isImporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                }
                                Text(if (uiState.isImporting) stringResource(R.string.importing) else stringResource(R.string.import_data))
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            // Encrypted backups section
                            Text(
                                text = stringResource(R.string.encrypted_backups),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.encrypted_backups_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Export Encrypted button
                            Button(
                                onClick = {
                                    val fileName = "myfinances_encrypted_${System.currentTimeMillis()}.json"
                                    exportEncryptedLauncher.launch(fileName)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isExporting && !uiState.isImporting
                            ) {
                                if (uiState.isExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                }
                                Text(if (uiState.isExporting) stringResource(R.string.exporting) else stringResource(R.string.export_encrypted))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Import Encrypted button
                            Button(
                                onClick = {
                                    importEncryptedLauncher.launch(arrayOf("application/json"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isExporting && !uiState.isImporting
                            ) {
                                if (uiState.isImporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                }
                                Text(if (uiState.isImporting) stringResource(R.string.importing) else stringResource(R.string.import_encrypted))
                            }

                            // Show export/import message
                            uiState.exportImportMessage?.let { message ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (message.contains("success", ignoreCase = true))
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (message.contains("success", ignoreCase = true))
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Password dialog for encrypted export
                    if (showExportPasswordDialog) {
                        PasswordDialog(
                            title = stringResource(R.string.encrypt_backup),
                            message = stringResource(R.string.encrypt_backup_message),
                            onConfirm = { password ->
                                showExportPasswordDialog = false
                                pendingExportUri?.let { uri ->
                                    viewModel.exportEncrypted(uri, password)
                                }
                                pendingExportUri = null
                            },
                            onDismiss = {
                                showExportPasswordDialog = false
                                pendingExportUri = null
                            }
                        )
                    }

                    // Password dialog for encrypted import
                    if (showImportPasswordDialog) {
                        PasswordDialog(
                            title = stringResource(R.string.decrypt_backup),
                            message = stringResource(R.string.decrypt_backup_message),
                            onConfirm = { password ->
                                showImportPasswordDialog = false
                                pendingImportUri?.let { uri ->
                                    viewModel.importEncrypted(uri, password, replaceExisting = false)
                                }
                                pendingImportUri = null
                            },
                            onDismiss = {
                                showImportPasswordDialog = false
                                pendingImportUri = null
                            }
                        )
                    }
                }

                // Dashboard Customization
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShapes.large,
                        elevation = secondaryCardElevation(),
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
                                text = stringResource(R.string.dashboard_customization),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.dashboard_customization_description),
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
                                            text = stringResource(R.string.card_portfolio_value),
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
                                            text = stringResource(R.string.card_portfolio_trend),
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
                                            text = stringResource(R.string.card_portfolio_distribution),
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
                                            text = stringResource(R.string.card_portfolio_growth),
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
                                            text = stringResource(R.string.card_best_worst_performers),
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
                        shape = CardShapes.large,
                        elevation = secondaryCardElevation(),
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
                                text = stringResource(R.string.notifications),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.notifications_description),
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
                                        text = if (uiState.notificationsEnabled) stringResource(R.string.enabled) else stringResource(R.string.disabled),
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
                                    text = stringResource(R.string.reminder_frequency),
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
                                                        3 -> stringResource(R.string.frequency_3_days)
                                                        7 -> stringResource(R.string.frequency_weekly)
                                                        14 -> stringResource(R.string.frequency_biweekly)
                                                        30 -> stringResource(R.string.frequency_monthly)
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
                        shape = CardShapes.large,
                        elevation = secondaryCardElevation(),
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
                                text = stringResource(R.string.exchange_rates),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.exchange_rates_description),
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
                                        stringResource(R.string.refreshing_rates)
                                    else
                                        stringResource(R.string.refresh_exchange_rates),
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
                                text = stringResource(R.string.last_updated,
                                    if (uiState.exchangeRates.isNotEmpty()) {
                                        val lastUpdated = uiState.exchangeRates.firstOrNull()?.lastUpdated ?: 0
                                        if (lastUpdated > 0) {
                                            java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                                                .format(java.util.Date(lastUpdated))
                                        } else stringResource(R.string.never)
                                    } else stringResource(R.string.never)
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Tools Section
                item {
                    Text(
                        text = stringResource(R.string.tools),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShapes.large,
                        elevation = secondaryCardElevation(),
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
                                text = stringResource(R.string.currency_converter),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.currency_converter_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    haptic.click()
                                    onNavigateToCurrencyConverter()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.open_converter))
                            }
                        }
                    }
                }

                // Mock Data Generator
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShapes.large,
                        elevation = secondaryCardElevation(),
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
                                text = stringResource(R.string.developer_tools),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.developer_tools_description),
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
                                        stringResource(R.string.generating_mock_data)
                                    else
                                        stringResource(R.string.generate_mock_data),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.mock_data_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordDialog(
    title: String,
    message: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isExport = title.contains("Encrypt", ignoreCase = true)

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isExport) androidx.compose.ui.text.input.ImeAction.Next else androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Confirm password field (only for export)
                if (isExport) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(stringResource(R.string.confirm_password)) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = confirmPassword.isNotEmpty() && password != confirmPassword
                    )
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text(
                            text = stringResource(R.string.passwords_do_not_match),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.isNotEmpty() && (!isExport || password == confirmPassword)) {
                        onConfirm(password)
                    }
                },
                enabled = password.isNotEmpty() && (!isExport || (password == confirmPassword && confirmPassword.isNotEmpty()))
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
