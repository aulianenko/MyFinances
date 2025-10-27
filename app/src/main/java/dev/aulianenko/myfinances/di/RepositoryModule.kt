package dev.aulianenko.myfinances.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.aulianenko.myfinances.data.dao.AccountDao
import dev.aulianenko.myfinances.data.dao.AccountValueDao
import dev.aulianenko.myfinances.data.dao.ExchangeRateDao
import dev.aulianenko.myfinances.data.repository.AccountRepository
import dev.aulianenko.myfinances.data.repository.ExchangeRateRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        accountValueDao: AccountValueDao
    ): AccountRepository {
        return AccountRepository(accountDao, accountValueDao)
    }

    @Provides
    @Singleton
    fun provideExchangeRateRepository(
        exchangeRateDao: ExchangeRateDao
    ): ExchangeRateRepository {
        return ExchangeRateRepository(exchangeRateDao)
    }
}
