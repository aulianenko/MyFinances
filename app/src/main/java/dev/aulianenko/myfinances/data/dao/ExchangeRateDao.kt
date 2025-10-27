package dev.aulianenko.myfinances.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.aulianenko.myfinances.data.entity.ExchangeRate
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates ORDER BY currencyCode ASC")
    fun getAllExchangeRates(): Flow<List<ExchangeRate>>

    @Query("SELECT * FROM exchange_rates WHERE currencyCode = :currencyCode")
    fun getExchangeRate(currencyCode: String): Flow<ExchangeRate?>

    @Query("SELECT * FROM exchange_rates WHERE currencyCode = :currencyCode")
    suspend fun getExchangeRateSync(currencyCode: String): ExchangeRate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRate(exchangeRate: ExchangeRate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRates(exchangeRates: List<ExchangeRate>)

    @Query("DELETE FROM exchange_rates WHERE currencyCode = :currencyCode")
    suspend fun deleteExchangeRate(currencyCode: String)

    @Query("DELETE FROM exchange_rates")
    suspend fun deleteAllExchangeRates()

    @Query("SELECT COUNT(*) FROM exchange_rates")
    suspend fun getExchangeRateCount(): Int
}
