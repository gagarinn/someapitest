package net.someapitest.domain.repositories

import kotlinx.coroutines.flow.Flow
import net.someapitest.domain.models.Amount
import net.someapitest.domain.models.Rates
import net.someapitest.domain.models.SupportedCurrency
import net.someapitest.domain.models.Transaction
import net.someapitest.domain.result.NetworkStatus

interface CurrencyRepository {

    suspend fun getBalance(): Flow<NetworkStatus<List<Amount>>>
    suspend fun getRates(): Flow<NetworkStatus<Rates>>
    suspend fun estimateExchange(from: Amount, to: SupportedCurrency): Flow<NetworkStatus<Transaction>>
    suspend fun submitExchange(submit: Transaction): Flow<NetworkStatus<Boolean>>
}