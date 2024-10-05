package net.someapitest.exchange

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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
import net.someapitest.domain.models.SupportedCurrency
import net.someapitest.domain.result.NetworkStatus
import net.someapitest.domain.usecases.GetBalanceUsecase
import net.someapitest.exchange.events.ExchangeEvents
import net.someapitest.ui.SingleEvent
import javax.inject.Inject

private const val INIT_AMOUNT = "0.00"

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val getBallanceUsecase: GetBalanceUsecase,
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

    private val _isLoadingRate = MutableStateFlow(true)
    val isLoadingRate get() = _isLoadingRate.asStateFlow()

    private val _hasItems = MutableStateFlow(false)
    val hasItems get() = _hasItems.asStateFlow()

    private val _isExchanging = MutableStateFlow(false)
    val isExchanging get() = _isExchanging.asStateFlow()

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

    fun onExchangeClick(){
        _exchangeAction.tryEmit(ExchangeEvents.OnExchangeClicked)
    }

    fun onToSellCurrencySelected(position: Int){
        Log.e("fuck", "-----onToSellCurrencySelected: ${SupportedCurrency.entries[position]}", )
        selectedToSellCurrency.update { SupportedCurrency.entries[position] }
    }

    fun onToReceiveCurrencySelected(position: Int){
        viewModelScope.launch {
            toReceiveCurrencies.collect{list ->
                Log.e("fuck", "-----onToReceiveCurrencySelected: ${list[position]}", )
                selectedToReceiveCurrency.update { list[position] }
            }
        }
    }

    fun onTextChanged(text: CharSequence) {
        _toSellAmount.update { text.toString() }
    }

    private fun handleBalance(result: NetworkStatus<List<Amount>>){
        when (result) {
            is NetworkStatus.Loading -> {
                _isLoadingBalance.update { true }
            }

            is NetworkStatus.Error -> {
                _isLoadingBalance.update { false }
            }

            is NetworkStatus.Success -> {
                _isLoadingBalance.update { false }
                _hasItems.update { result.data.orEmpty().isNotEmpty() }
                _exchangeAction.tryEmit(ExchangeEvents.OnBalanceUpdated(result.data.orEmpty()))
            }
        }
    }
}
