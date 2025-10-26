package dev.aulianenko.myfinances.data.repository

import dev.aulianenko.myfinances.data.dao.AccountDao
import dev.aulianenko.myfinances.data.dao.AccountValueDao
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.entity.AccountValue
import kotlinx.coroutines.flow.Flow

class AccountRepository(
    private val accountDao: AccountDao,
    private val accountValueDao: AccountValueDao
) {
    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()

    fun getAccountById(accountId: String): Flow<Account?> = accountDao.getAccountById(accountId)

    suspend fun insertAccount(account: Account) = accountDao.insertAccount(account)

    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: Account) = accountDao.deleteAccount(account)

    suspend fun getAccountCount(): Int = accountDao.getAccountCount()

    fun getAccountValues(accountId: String): Flow<List<AccountValue>> =
        accountValueDao.getAccountValues(accountId)

    fun getLatestAccountValue(accountId: String): Flow<AccountValue?> =
        accountValueDao.getLatestAccountValue(accountId)

    fun getAccountValuesInPeriod(
        accountId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<AccountValue>> =
        accountValueDao.getAccountValuesInPeriod(accountId, startTime, endTime)

    fun getAllAccountValuesInPeriod(
        startTime: Long,
        endTime: Long
    ): Flow<List<AccountValue>> =
        accountValueDao.getAllAccountValuesInPeriod(startTime, endTime)

    suspend fun insertAccountValue(accountValue: AccountValue) =
        accountValueDao.insertAccountValue(accountValue)

    suspend fun updateAccountValue(accountValue: AccountValue) =
        accountValueDao.updateAccountValue(accountValue)

    suspend fun deleteAccountValue(accountValue: AccountValue) =
        accountValueDao.deleteAccountValue(accountValue)
}
