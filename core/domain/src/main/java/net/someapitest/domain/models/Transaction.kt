package net.someapitest.domain.models

data class Transaction(
    val from: Amount,
    val to: Amount,
    val commission: Amount? = null
){
    init {
        commission?.let {
            if (from.currency != it.currency){
                throw IllegalArgumentException()
            }
        }
    }
}