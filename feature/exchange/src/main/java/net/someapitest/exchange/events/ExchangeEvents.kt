package net.someapitest.exchange.events

import net.someapitest.domain.models.Amount
import net.someapitest.ui.SingleEvent

sealed class ExchangeEvents: SingleEvent {
    data class OnBalanceUpdated(val data: List<Amount>): SingleEvent
    object OnExchangeClicked: SingleEvent
    object OnSubmitClicked: SingleEvent
}