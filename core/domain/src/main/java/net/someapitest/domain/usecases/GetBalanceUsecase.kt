package net.someapitest.domain.usecases

import kotlinx.coroutines.flow.Flow
import net.someapitest.domain.models.Amount
import net.someapitest.domain.repositories.CurrencyRepository
import net.someapitest.domain.result.NetworkStatus
import javax.inject.Inject

class GetBalanceUsecase @Inject constructor(private val repository: CurrencyRepository) {
    suspend fun invoke(): Flow<NetworkStatus<List<Amount>>> = repository.getBalance()
}