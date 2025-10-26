package dev.aulianenko.myfinances.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.aulianenko.myfinances.data.entity.AccountValue
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountValueDao {
    @Query("SELECT * FROM account_values WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getAccountValues(accountId: String): Flow<List<AccountValue>>

    @Query("SELECT * FROM account_values WHERE accountId = :accountId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAccountValue(accountId: String): Flow<AccountValue?>

    @Query("SELECT * FROM account_values WHERE accountId = :accountId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getAccountValuesInPeriod(accountId: String, startTime: Long, endTime: Long): Flow<List<AccountValue>>

    @Query("SELECT * FROM account_values WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getAllAccountValuesInPeriod(startTime: Long, endTime: Long): Flow<List<AccountValue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountValue(accountValue: AccountValue)

    @Update
    suspend fun updateAccountValue(accountValue: AccountValue)

    @Delete
    suspend fun deleteAccountValue(accountValue: AccountValue)

    @Query("DELETE FROM account_values WHERE accountId = :accountId")
    suspend fun deleteAccountValues(accountId: String)
}