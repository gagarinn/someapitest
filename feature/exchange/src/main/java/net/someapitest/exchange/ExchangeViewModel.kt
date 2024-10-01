package net.someapitest.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.someapitest.domain.models.Amount
import net.someapitest.domain.result.NetworkStatus
import net.someapitest.domain.usecases.GetBalanceUsecase
import net.someapitest.ui.SingleEvent
import javax.inject.Inject

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val getBallanceUsecase: GetBalanceUsecase,
) : ViewModel() {

    private val _isLoadingBalance = MutableStateFlow(true)
    val isLoadingBalance get() = _isLoadingBalance.asStateFlow()

    private val _hasItems = MutableStateFlow(false)
    val hasItems get() = _hasItems.asStateFlow()

    private val _devicesAction = MutableSharedFlow<SingleEvent>(extraBufferCapacity = 1)
    val devicesAction = _devicesAction.asSharedFlow()

    fun getBalance() {
        viewModelScope.launch {
            getBallanceUsecase.invoke().collect { result ->
                handleBalance(result)
            }
        }
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
                _devicesAction.tryEmit(ExchangeEvents.OnBalanceUpdated(result.data.orEmpty()))
            }
        }
    }
}
