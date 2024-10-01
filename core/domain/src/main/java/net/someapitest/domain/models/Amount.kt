package net.someapitest.domain.models

import java.text.DecimalFormat

private const val ZERO_VALUE = "0.00"

data class Amount(
    val value: Double,
    val currency: SupportedCurrency
) {
    val formattedValue: String
        get() = if (value == 0.0) ZERO_VALUE else DecimalFormat("#.00").format(value)
}