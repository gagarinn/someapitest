package net.someapitest.data.api

import net.someapitest.domain.models.Rates
import retrofit2.Response
import retrofit2.http.GET

interface CurrencyApi {

    @GET("tasks/api/currency-exchange-rates")
    suspend fun fetchDevices(): Response<Rates>
}