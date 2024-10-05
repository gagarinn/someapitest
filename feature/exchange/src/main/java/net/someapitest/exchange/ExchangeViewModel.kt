package net.someapitest.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.someapitest.domain.models.Amount
import net.someapitest.domain.models.Rates
import net.someapitest.domain.models.SupportedCurrency
import net.someapitest.domain.result.NetworkStatus
import net.someapitest.domain.usecases.EstimateExchangeUsecase
import net.someapitest.domain.usecases.GetBalanceUsecase
import net.someapitest.domain.usecases.GetExchangeRateUsecase
import net.someapitest.exchange.events.ExchangeEvents
import net.someapitest.ui.SingleEvent
import javax.inject.Inject

private const val INIT_AMOUNT = "0.00"
private const val DEFAULT_UPDATE_RATES_MILISECONDS = 5000L

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val getBallanceUsecase: GetBalanceUsecase,
    private val getExchangeRateUsecase: GetExchangeRateUsecase,
    private val estimateExchangeUsecase: EstimateExchangeUsecase,
) : ViewModel() {

    val scope: CoroutineScope
        get() = viewModelScope

    val toSellCurrencies = SupportedCurrency.entries
    private val selectedToSellCurrency = MutableStateFlow(SupportedCurrency.EUR)
    private val selectedToReceiveCurrency = MutableStateFlow(SupportedCurrency.USD)
    val toReceiveCurrencies = selectedToSellCurrency
        .map { selectedCurrency ->
            toSellCurrencies.filterNot { it == selectedCurrency }
        }
        .distinctUntilChanged()

    private val _toSellAmount = MutableStateFlow(INIT_AMOUNT)
    val toSellAmount get() = _toSellAmount.asStateFlow()

    private val _toReceivedAmount = MutableStateFlow(INIT_AMOUNT)
    val toReceivedAmount: StateFlow<String> = _toReceivedAmount
        .map { amount -> "+$amount" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "+${INIT_AMOUNT}"
        )

    private val _isLoadingBalance = MutableStateFlow(true)
    val isLoadingBalance get() = _isLoadingBalance.asStateFlow()

    private val _isLoadingRate = MutableStateFlow(false)
    val isLoadingRate get() = _isLoadingRate.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError get() = _hasError.asStateFlow()

    private val _isExchanging = MutableStateFlow(false)
    val isExchanging get() = _isExchanging.asStateFlow()

    private var exchangeRateJob: Job? = null
    private var currentRates: Rates? = null

    private val _exchangeAction = MutableSharedFlow<SingleEvent>(extraBufferCapacity = 1)
    val exchangeAction = _exchangeAction.asSharedFlow()

    init {
        toReceiveCurrencies.onEach { currencies ->
            selectedToReceiveCurrency.value = currencies.firstOrNull() ?: SupportedCurrency.USD
        }.launchIn(viewModelScope)
    }

    fun getBalance() {
        viewModelScope.launch {
            getBallanceUsecase.invoke().collect { result ->
                handleBalance(result)
            }
        }
    }

    fun onExchangeClick() {
        if (_isExchanging.value) {
            _exchangeAction.tryEmit(ExchangeEvents.OnSubmitClicked)
            submit()
            stopExchanging()
        } else {
            _isExchanging.update { true }
            _exchangeAction.tryEmit(ExchangeEvents.OnExchangeClicked)
            startExchanging()
        }
    }

    fun onToSellCurrencySelected(position: Int) {
        selectedToSellCurrency.update { SupportedCurrency.entries[position] }
    }

    fun onToReceiveCurrencySelected(position: Int) {
        viewModelScope.launch {
            toReceiveCurrencies.collect { list ->
                selectedToReceiveCurrency.update { list[position] }
            }
        }
    }

    fun onTextChanged(text: CharSequence) {
        _toSellAmount.update { text.toString() }
        if (text.toString() != INIT_AMOUNT) {
            text.toString().toDoubleOrNull()?.let {
                viewModelScope.launch {
                    estimateExchangeUsecase.invoke(
                        Amount(
                            value = it,
                            currency = selectedToSellCurrency.value
                        ), to = selectedToReceiveCurrency.value
                    ).collect { result ->
                        when (result) {
                            is NetworkStatus.Loading -> Unit
                            is NetworkStatus.Error -> {
                                _exchangeAction.tryEmit(ExchangeEvents.OnError(result.errorMessage))
                                _toReceivedAmount.update { INIT_AMOUNT }
                                _hasError.update { true }
                            }

                            is NetworkStatus.Success -> result.data?.second?.formattedValue?.let { value ->
                                _toReceivedAmount.update { value }
                                _hasError.update { false }
                            }
                        }
                    }
                }
            }
        } else {
            _toReceivedAmount.update { INIT_AMOUNT }
            _hasError.update { false }
        }
    }

    private fun stopExchanging() {
        exchangeRateJob?.cancel()
    }

    private fun startExchanging() {
        exchangeRateJob = viewModelScope.launch {
            while (true) {
                updateRates()
                delay(DEFAULT_UPDATE_RATES_MILISECONDS)
            }
        }
        exchangeRateJob?.start()
    }

    private suspend fun updateRates() {
        getExchangeRateUsecase.invoke().collect { result ->
            when (result) {
                is NetworkStatus.Loading -> _isLoadingRate.update { true }
                is NetworkStatus.Error -> _isLoadingRate.update { false }
                is NetworkStatus.Success -> {
                    _isLoadingRate.update { false }
                    currentRates = result.data
                }
            }
        }
    }

    private fun submit() {
//        temp update
        _isExchanging.update { false }
    }

    private fun handleBalance(result: NetworkStatus<List<Amount>>) {
        when (result) {
            is NetworkStatus.Loading -> _isLoadingBalance.update { true }
            is NetworkStatus.Error -> _isLoadingBalance.update { false }
            is NetworkStatus.Success -> {
                _isLoadingBalance.update { false }
                _exchangeAction.tryEmit(ExchangeEvents.OnBalanceUpdated(result.data.orEmpty()))
            }
        }
    }
}
