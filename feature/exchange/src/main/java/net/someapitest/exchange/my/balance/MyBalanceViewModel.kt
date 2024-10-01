package net.someapitest.exchange.my.balance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.someapitest.domain.models.Amount

class MyBalanceViewModel(private val amount: Amount) {

    private val _title = MutableStateFlow("")
    val title get() = _title.asStateFlow()

    init {
        _title.update { amount.formattedValue + " " + amount.currency.name }
    }
}