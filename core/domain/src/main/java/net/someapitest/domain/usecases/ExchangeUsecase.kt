package net.someapitest.domain.usecases

import kotlinx.coroutines.flow.Flow
import net.someapitest.domain.models.Rates
import net.someapitest.domain.models.SupportedCurrency
import net.someapitest.domain.result.NetworkStatus

interface ExchangeUsecase {
    suspend fun invoke(amount: Double, from: SupportedCurrency, to: SupportedCurrency): Flow<NetworkStatus<Rates>>
}