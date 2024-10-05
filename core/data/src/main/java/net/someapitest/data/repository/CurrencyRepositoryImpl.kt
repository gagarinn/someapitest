package net.someapitest.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import net.someapitest.data.datastore.CurrencyDataStore
import net.someapitest.domain.models.Amount
import net.someapitest.domain.models.Rates
import net.someapitest.domain.models.SupportedCurrency
import net.someapitest.domain.repositories.CurrencyRepository
import net.someapitest.domain.result.NetworkStatus
import javax.inject.Inject

private const val DEFAULT_AMOUNT = 0.0
private const val DEFAULT_AMOUNT_EUR = 1000.0

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyDataStore: CurrencyDataStore
) : CurrencyRepository {

    private var balance = SupportedCurrency.entries.map { currency ->
        if (currency == SupportedCurrency.EUR) {
            Amount(DEFAULT_AMOUNT_EUR, currency)
        } else {
            Amount(DEFAULT_AMOUNT, currency)
        }

    }.toMutableList()

    override suspend fun getBalance(): Flow<NetworkStatus<List<Amount>>> {
        delay(1000)
        return flow<NetworkStatus<List<Amount>>> { emit(NetworkStatus.Success(balance)) }
            .onStart { emit(NetworkStatus.Loading()) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getRates(): Flow<NetworkStatus<Rates>> {
        return flow { emit(currencyDataStore.getRates()) }
            .onStart { emit(NetworkStatus.Loading()) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun updateBalance(amounts: List<Amount>) {
        balance = amounts.toMutableList()
    }
}