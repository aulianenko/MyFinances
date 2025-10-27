package dev.aulianenko.myfinances.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Exchange rate entity for currency conversions.
 * Stores exchange rates from a base currency (USD) to target currencies.
 */
@Entity(
    tableName = "exchange_rates",
    indices = [Index(value = ["currencyCode"], unique = true)]
)
data class ExchangeRate(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val currencyCode: String,
    val rateToUSD: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
