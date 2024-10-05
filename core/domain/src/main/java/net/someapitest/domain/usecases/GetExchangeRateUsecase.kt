package net.someapitest.domain.usecases

import kotlinx.coroutines.flow.Flow
import net.someapitest.domain.models.Rates
import net.someapitest.domain.repositories.CurrencyRepository
import net.someapitest.domain.result.NetworkStatus
import javax.inject.Inject

class GetExchangeRateUsecase @Inject constructor(private val repository: CurrencyRepository) {
    suspend fun invoke(): Flow<NetworkStatus<Rates>> = repository.getRates()
}