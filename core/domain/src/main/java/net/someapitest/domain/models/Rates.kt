package net.someapitest.domain.models

data class Rates(
    val base: SupportedCurrency,
    val date: String,
    val rates: List<ExchangeRate>
)