package net.someapitest.domain.usecases

import kotlinx.coroutines.flow.Flow
import net.someapitest.domain.models.Amount
import net.someapitest.domain.models.SupportedCurrency
import net.someapitest.domain.repositories.CurrencyRepository
import net.someapitest.domain.result.NetworkStatus
import javax.inject.Inject

class EstimateExchangeUsecase @Inject constructor(private val repository: CurrencyRepository) {
    suspend fun invoke(amount: Amount, to: SupportedCurrency): Flow<NetworkStatus<Pair<Amount,Amount>>> {
        return repository.estimateExchange(amount, to)
    }
}