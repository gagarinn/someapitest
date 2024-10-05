package net.someapitest.ui

import android.view.View
import android.widget.Spinner
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.someapitest.domain.models.SupportedCurrency

object BindingAdapters {

    @JvmStatic
    @BindingAdapter("android:isVisible")
    fun View.setVisibility(value: Boolean?) {
        isVisible = value ?: false
    }

    @JvmStatic
    @BindingAdapter(value = ["currencies"])
    fun Spinner.setCurrencies(currencies: List<SupportedCurrency>?) {
        currencies?.let {
            adapter =
                CurrencySpinnerAdapter(context, android.R.layout.simple_spinner_dropdown_item, it)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["currencies", "scope"])
    fun Spinner.setCurrenciesFlow(currenciesFlow: Flow<List<SupportedCurrency>?>, scope: CoroutineScope) {
        scope.launch {
            currenciesFlow.collect { list ->
                list?.let {
                    adapter = CurrencySpinnerAdapter(context, android.R.layout.simple_spinner_dropdown_item, it)
                }
            }
        }
    }
}