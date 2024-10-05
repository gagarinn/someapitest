package net.someapitest.data.datastore

import net.someapitest.data.api.CurrencyApi
import net.someapitest.domain.models.Rates
import net.someapitest.domain.result.NetworkStatus

class CurrencyDataStoreImpl(private val api: CurrencyApi) : CurrencyDataStore {

    override suspend fun getRates(): NetworkStatus<Rates> {
        return try {
            val response = api.fetchDevices()
            when {
                response.isSuccessful -> NetworkStatus.Success(response.body())
                else -> NetworkStatus.Error(errorMessage = response.message())
            }
        } catch (e: Exception) {
            NetworkStatus.Error(errorMessage = "Unknown error")
        }
    }
}