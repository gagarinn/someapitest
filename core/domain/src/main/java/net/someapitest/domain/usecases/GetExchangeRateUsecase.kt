package net.someapitest.domain.usecases

import kotlinx.coroutines.flow.Flow
import net.someapitest.domain.models.Rates
import net.someapitest.domain.result.NetworkStatus

interface GetExchangeRateUsecase {
    suspend fun invoke(): Flow<NetworkStatus<Rates>>
}