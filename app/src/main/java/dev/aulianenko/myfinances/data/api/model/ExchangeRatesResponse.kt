package dev.aulianenko.myfinances.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response model for Frankfurter API /latest endpoint
 * Example response:
 * {
 *   "base": "USD",
 *   "date": "2025-10-28",
 *   "rates": {
 *     "EUR": 0.85,
 *     "GBP": 0.75,
 *     ...
 *   }
 * }
 */
@JsonClass(generateAdapter = true)
data class ExchangeRatesResponse(
    @Json(name = "base")
    val base: String,

    @Json(name = "date")
    val date: String,

    @Json(name = "rates")
    val rates: Map<String, Double>
)
