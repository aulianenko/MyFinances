package dev.aulianenko.myfinances.data.export

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.aulianenko.myfinances.data.dao.AccountDao
import dev.aulianenko.myfinances.data.dao.AccountValueDao
import dev.aulianenko.myfinances.data.dao.ExchangeRateDao
import dev.aulianenko.myfinances.data.database.AppDatabase
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.entity.AccountValue
import dev.aulianenko.myfinances.data.entity.ExchangeRate
import dev.aulianenko.myfinances.security.EncryptedData
import dev.aulianenko.myfinances.security.EncryptionUtil
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for exporting and importing user data.
 * Handles serialization to JSON and file I/O operations.
 */
@Singleton
class ExportImportRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val accountDao: AccountDao,
    private val accountValueDao: AccountValueDao,
    private val exchangeRateDao: ExchangeRateDao
) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val exportDataAdapter = moshi.adapter(ExportData::class.java).indent("  ")
    private val encryptedDataAdapter = moshi.adapter(EncryptedData::class.java).indent("  ")

    /**
     * Get the app version name from PackageManager.
     */
    private fun getAppVersion(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    /**
     * Export all user data to JSON format.
     * @return ExportData object containing all data and metadata
     */
    suspend fun exportData(): ExportData {
        val accounts = accountDao.getAllAccounts().first()
        val accountValues = mutableListOf<AccountValue>()

        // Collect all account values for all accounts
        accounts.forEach { account ->
            val values = accountValueDao.getAccountValues(account.id).first()
            accountValues.addAll(values)
        }

        val exchangeRates = exchangeRateDao.getAllExchangeRates().first()

        return ExportData(
            metadata = ExportMetadata(
                appVersion = getAppVersion(),
                exportDate = System.currentTimeMillis(),
                formatVersion = 1,
                encrypted = false
            ),
            accounts = accounts,
            accountValues = accountValues,
            exchangeRates = exchangeRates
        )
    }

    /**
     * Export data to a JSON string.
     * @return JSON string representation of all data
     */
    suspend fun exportToJson(): String {
        val data = exportData()
        return exportDataAdapter.toJson(data)
    }

    /**
     * Export data to a file via URI (for use with Storage Access Framework).
     * @param uri The URI of the file to write to
     * @return Result indicating success or failure
     */
    suspend fun exportToFile(uri: Uri): Result<Unit> {
        return try {
            val json = exportToJson()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            } ?: throw IOException("Failed to open output stream")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Import data from a JSON string.
     * @param json The JSON string to import
     * @param replaceExisting If true, existing data will be replaced; if false, data will be merged
     * @return Result indicating success or failure with number of imported items
     */
    suspend fun importFromJson(json: String, replaceExisting: Boolean = false): Result<ImportResult> {
        return try {
            val data = exportDataAdapter.fromJson(json)
                ?: throw IOException("Failed to parse JSON data")

            // Validate format version
            if (data.metadata.formatVersion > 1) {
                throw IOException("Unsupported format version: ${data.metadata.formatVersion}")
            }

            // Perform import within a transaction to ensure atomicity
            // If any operation fails, all changes will be rolled back
            database.withTransaction {
                if (replaceExisting) {
                    // Delete existing data first
                    clearAllData()
                }

                // Import accounts
                data.accounts.forEach { account ->
                    accountDao.insertAccount(account)
                }

                // Import account values
                data.accountValues.forEach { accountValue ->
                    accountValueDao.insertAccountValue(accountValue)
                }

                // Import exchange rates
                exchangeRateDao.insertExchangeRates(data.exchangeRates)
            }

            Result.success(
                ImportResult(
                    accountsImported = data.accounts.size,
                    accountValuesImported = data.accountValues.size,
                    exchangeRatesImported = data.exchangeRates.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Import data from a file via URI (for use with Storage Access Framework).
     * @param uri The URI of the file to read from
     * @param replaceExisting If true, existing data will be replaced; if false, data will be merged
     * @return Result indicating success or failure with number of imported items
     */
    suspend fun importFromFile(uri: Uri, replaceExisting: Boolean = false): Result<ImportResult> {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw IOException("Failed to open input stream")

            importFromJson(json, replaceExisting)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export data to an encrypted file with password protection.
     * @param uri The URI to write the encrypted file to
     * @param password The password to use for encryption
     * @return Result indicating success or failure
     */
    suspend fun exportEncrypted(uri: Uri, password: String): Result<Unit> {
        return try {
            // Get unencrypted JSON
            val json = exportToJson()

            // Encrypt the JSON
            val encryptedData = EncryptionUtil.encrypt(json, password)

            // Convert encrypted data to JSON
            val encryptedJson = encryptedDataAdapter.toJson(encryptedData)

            // Write to file
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(encryptedJson.toByteArray())
            } ?: throw IOException("Failed to open output stream")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Import data from an encrypted file with password protection.
     * @param uri The URI to read the encrypted file from
     * @param password The password to use for decryption
     * @param replaceExisting If true, existing data will be replaced; if false, data will be merged
     * @return Result indicating success or failure with number of imported items
     */
    suspend fun importEncrypted(
        uri: Uri,
        password: String,
        replaceExisting: Boolean = false
    ): Result<ImportResult> {
        return try {
            // Read encrypted JSON from file
            val encryptedJson = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw IOException("Failed to open input stream")

            // Parse encrypted data
            val encryptedData = encryptedDataAdapter.fromJson(encryptedJson)
                ?: throw IOException("Failed to parse encrypted data")

            // Decrypt the data
            val json = try {
                EncryptionUtil.decrypt(encryptedData, password)
            } catch (e: Exception) {
                throw IOException("Failed to decrypt data. Wrong password?", e)
            }

            // Import the decrypted JSON
            importFromJson(json, replaceExisting)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear all data from the database.
     * Used before importing when replaceExisting is true.
     */
    private suspend fun clearAllData() {
        // Get all accounts and delete them (cascade will delete values)
        val accounts = accountDao.getAllAccounts().first()
        accounts.forEach { account ->
            accountDao.deleteAccount(account)
        }

        // Clear exchange rates
        exchangeRateDao.deleteAllExchangeRates()
    }
}

/**
 * Result of an import operation.
 */
data class ImportResult(
    val accountsImported: Int,
    val accountValuesImported: Int,
    val exchangeRatesImported: Int
)
