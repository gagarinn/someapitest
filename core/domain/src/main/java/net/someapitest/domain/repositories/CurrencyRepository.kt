package net.someapitest.domain.repositories

import kotlinx.coroutines.flow.Flow
import net.someapitest.domain.models.Amount
import net.someapitest.domain.result.NetworkStatus

interface CurrencyRepository {

    suspend fun getBalance(): Flow<NetworkStatus<List<Amount>>>
    suspend fun updateBalance(amounts: List<Amount>)
}