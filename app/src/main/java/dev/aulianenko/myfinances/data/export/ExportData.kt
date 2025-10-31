package dev.aulianenko.myfinances.data.export

import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.data.entity.ExchangeRate

/**
 * Data class representing a complete export of all user data.
 * This format is used for both regular exports and encrypted backups.
 */
data class ExportData(
    val metadata: ExportMetadata,
    val accounts: List<Account>,
    val accountValues: List<AccountValue>,
    val exchangeRates: List<ExchangeRate>
)

/**
 * Metadata about the export, including version and timestamp.
 */
data class ExportMetadata(
    val appVersion: String,
    val exportDate: Long,
    val formatVersion: Int = 1,
    val encrypted: Boolean = false
)
