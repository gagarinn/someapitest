package net.someapitest.exchange.events

import net.someapitest.domain.models.Amount
import net.someapitest.domain.models.Transaction
import net.someapitest.ui.SingleEvent

sealed class ExchangeEvents: SingleEvent {
    data class OnBalanceUpdated(val data: List<Amount>): SingleEvent
    data class OnSubmited(val transaction: Transaction): SingleEvent
    data class OnError(val message: String?): SingleEvent
    object OnExchangeClicked: SingleEvent
    object OnSubmitClicked: SingleEvent
}