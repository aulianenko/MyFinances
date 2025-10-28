package dev.aulianenko.myfinances.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.aulianenko.myfinances.domain.usecase.CurrencyConversionUseCase

/**
 * WorkManager worker that periodically updates exchange rates from API
 */
@HiltWorker
class ExchangeRateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val currencyConversionUseCase: CurrencyConversionUseCase
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ExchangeRateWorker"
        const val WORK_NAME = "exchange_rate_update_work"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting exchange rate update worker")

            val result = currencyConversionUseCase.updateExchangeRatesFromApi()

            result.fold(
                onSuccess = { count ->
                    Log.d(TAG, "Successfully updated $count exchange rates")
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to update exchange rates: ${error.message}", error)
                    // Retry on failure
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing exchange rate worker", e)
            Result.retry()
        }
    }
}
