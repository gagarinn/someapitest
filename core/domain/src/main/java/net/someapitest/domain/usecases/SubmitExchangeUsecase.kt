package net.someapitest.domain.usecases

import kotlinx.coroutines.flow.Flow
import net.someapitest.domain.models.Transaction
import net.someapitest.domain.repositories.CurrencyRepository
import net.someapitest.domain.result.NetworkStatus
import javax.inject.Inject

class SubmitExchangeUsecase @Inject constructor(private val repository: CurrencyRepository) {
    suspend fun invoke(submit: Transaction): Flow<NetworkStatus<Boolean>> {
        return repository.submitExchange(submit)
    }
}