package dev.aulianenko.myfinances.data.api

import dev.aulianenko.myfinances.data.api.model.ExchangeRatesResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for Frankfurter API
 * Documentation: https://frankfurter.dev
 */
interface FrankfurterApiService {

    /**
     * Get latest exchange rates from Frankfurter API
     * @param base Base currency for exchange rates (default: USD)
     * @return Exchange rates response with all available currencies
     */
    @GET("latest")
    suspend fun getLatestRates(
        @Query("base") base: String = "USD"
    ): ExchangeRatesResponse
}
