package net.someapitest.domain.models

data class ExchangeRate(
    val rate: Double,
    val currency: SupportedCurrency
)