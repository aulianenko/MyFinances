package dev.aulianenko.myfinances

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.aulianenko.myfinances.domain.usecase.CurrencyConversionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyFinancesApplication : Application() {

    @Inject
    lateinit var currencyConversionUseCase: CurrencyConversionUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        initializeExchangeRates()
    }

    private fun initializeExchangeRates() {
        applicationScope.launch {
            currencyConversionUseCase.initializeExchangeRates()
        }
    }
}
