package net.someapitest.data.datastore

import net.someapitest.domain.models.Rates
import net.someapitest.domain.result.NetworkStatus

interface CurrencyDataStore {

    suspend fun getRates(): NetworkStatus<Rates>
}