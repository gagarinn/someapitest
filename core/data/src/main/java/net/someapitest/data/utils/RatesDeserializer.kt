package net.someapitest.data.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.someapitest.domain.models.ExchangeRate
import net.someapitest.domain.models.Rates
import net.someapitest.domain.models.SupportedCurrency
import java.lang.reflect.Type

private const val BASE_KEY = "base"
private const val DATE_KEY = "date"
private const val RATES_KEY = "rates"

class RatesDeserializer : JsonDeserializer<Rates> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Rates {
        val jsonObject = json.asJsonObject

        val base = SupportedCurrency.valueOf(jsonObject[BASE_KEY].asString)
        val date = jsonObject[DATE_KEY].asString

        val ratesObject = jsonObject[RATES_KEY].asJsonObject
        val rates = ratesObject.entrySet().mapNotNull { entry ->
            try {
                val currency = SupportedCurrency.valueOf(entry.key)
                val rate = entry.value.asDouble
                ExchangeRate(rate, currency)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        return Rates(base, date, rates)
    }
}