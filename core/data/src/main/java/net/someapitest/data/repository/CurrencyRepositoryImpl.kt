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
private const val NUMBERS_OF_FREE_OPERATIONS = 5
private const val DEFAULT_FEE = 0.007

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

    private var currentRates: Rates? = null
    private var numberOfOperation = 0L

    override suspend fun getBalance(): Flow<NetworkStatus<List<Amount>>> {
        delay(1000)
        return flow<NetworkStatus<List<Amount>>> { emit(NetworkStatus.Success(balance)) }
            .onStart { emit(NetworkStatus.Loading()) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getRates(): Flow<NetworkStatus<Rates>> {
        val rates = currencyDataStore.getRates()
        rates.data?.let {
            currentRates = it
        }
        return flow { emit(rates) }
            .onStart { emit(NetworkStatus.Loading()) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun updateBalance(amounts: List<Amount>) {
        balance = amounts.toMutableList()
    }

    override suspend fun estimateExchange(
        amount: Amount,
        to: SupportedCurrency
    ): Flow<NetworkStatus<Pair<Amount, Amount>>> {
        var result: Pair<Amount, Amount>? = null
        var errorMessage: String? = null
        val rate = findRate(amount.currency, to)

        if (rate == null) {
            errorMessage = "Unknown rate"
        }
        rate?.let {
            val estimatedValue = findValue(amount, it, to)
            if (isEnoughMoney(estimatedValue.first)) {
                result = estimatedValue
            } else {
                errorMessage = "Not enough money"
            }
        }

        return flow {
            emit(
                if (result != null) NetworkStatus.Success(result) else NetworkStatus.Error(
                    errorMessage = errorMessage
                )
            )
        }
            .onStart { emit(NetworkStatus.Loading()) }
            .flowOn(Dispatchers.IO)
    }

    private fun isEnoughMoney(estimated: Amount): Boolean {
        return balance.first { estimated.currency == it.currency }.value - estimated.value >= 0
    }

    private fun findValue(
        amount: Amount,
        rate: Double,
        to: SupportedCurrency
    ): Pair<Amount, Amount> {
        return if (numberOfOperation > NUMBERS_OF_FREE_OPERATIONS) {
            val resultCurrent = Amount(
                value = amount.value + amount.value * DEFAULT_FEE,
                currency = amount.currency
            )
            val resultTo = Amount(value = amount.value * rate * DEFAULT_FEE, currency = to)
            Pair(resultCurrent, resultTo)
        } else {
            val resultCurrent = amount
            val resultTo = Amount(value = amount.value * rate, currency = to)
            Pair(resultCurrent, resultTo)
        }
    }

    private fun findRate(from: SupportedCurrency, to: SupportedCurrency): Double? {
        var result: Double? = null
        val rateTo = currentRates?.rates?.first { it.currency == to }?.rate
        if (from == SupportedCurrency.EUR) {
            result = rateTo
        } else {
            rateTo?.let {
                result = (currentRates?.rates?.first { it.currency == from }?.rate)?.times(it)
            }
        }
        return result
    }
}