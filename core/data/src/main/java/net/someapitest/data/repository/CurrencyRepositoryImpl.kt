package net.someapitest.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import net.someapitest.data.datastore.CurrencyDataStore
import net.someapitest.domain.models.Amount
import net.someapitest.domain.models.Rates
import net.someapitest.domain.models.SupportedCurrency
import net.someapitest.domain.models.Transaction
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

    private var balance = SupportedCurrency.entries.associate { currency ->
        currency to if (currency == SupportedCurrency.EUR) {
            DEFAULT_AMOUNT_EUR
        } else {
            DEFAULT_AMOUNT
        }
    }.toMutableMap()

    private var currentRates: Rates? = null
    private var numberOfOperation = 0L

    override suspend fun getBalance(): Flow<NetworkStatus<List<Amount>>> {
        return flow<NetworkStatus<List<Amount>>> { emit(NetworkStatus.Success(balance.map {Amount(it.value, it.key)  })) }
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

    override suspend fun estimateExchange(
        from: Amount,
        to: SupportedCurrency
    ): Flow<NetworkStatus<Transaction>> {
        var result: Transaction? = null
        var errorMessage: String? = null
        val rate = findRate(from.currency, to)

        if (rate == null) {
            errorMessage = "Unknown rate"
        }
        rate?.let {
            val estimatedValue = prepareTransaction(from, it, to)
            if (isEnoughMoney(estimatedValue)) {
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

    override suspend fun submitExchange(submit: Transaction): Flow<NetworkStatus<Boolean>> {
        val currentBalance =  balance[submit.from.currency]?: 0.0
        var restOfBalance = currentBalance - submit.from.value
        submit.commission?.value?.let { commission ->
            restOfBalance -= commission
        }
        var success = false
        if (restOfBalance >= 0){
            balance[submit.from.currency] = restOfBalance
            val currentToBalance =  balance[submit.to.currency]?: 0.0
            balance[submit.to.currency] = currentToBalance + submit.to.value
            success = true
            numberOfOperation++
        }
        return flow { emit(NetworkStatus.Success(success)) }
            .flowOn(Dispatchers.IO)
    }

    private fun isEnoughMoney(estimated: Transaction): Boolean {
        balance[estimated.from.currency]?.let {
            return it - estimated.from.value - (estimated.commission?.value ?: 0.0) >= 0
        }
        return false
    }

    private fun prepareTransaction(
        amount: Amount,
        rate: Double,
        to: SupportedCurrency
    ): Transaction {
        return if (numberOfOperation >= NUMBERS_OF_FREE_OPERATIONS) {
            val resultTo = Amount(value = amount.value * rate, currency = to)
            val commission = Amount(value = amount.value * DEFAULT_FEE, currency = amount.currency)
            Transaction(from = amount, to = resultTo, commission = commission)
        } else {
            val resultTo = Amount(value = amount.value * rate, currency = to)
            Transaction(from = amount, to = resultTo, commission = null)
        }
    }

    private fun findRate(from: SupportedCurrency, to: SupportedCurrency): Double? {

        if (from == to) return 1.0

       // Find the rate for both currencies relative to EUR
        val fromRate = currentRates?.rates?.find { it.currency == from }?.rate
        val toRate = currentRates?.rates?.find { it.currency == to }?.rate

        return when {
            from == SupportedCurrency.EUR -> toRate
            to == SupportedCurrency.EUR -> fromRate?.let { 1 / it }
            fromRate != null && toRate != null -> toRate / fromRate
            else -> null
        }
    }
}