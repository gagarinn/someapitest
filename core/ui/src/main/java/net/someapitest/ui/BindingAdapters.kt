package net.someapitest.ui

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

object BindingAdapters {

    @JvmStatic
    @BindingAdapter("android:isVisible")
    fun View.setVisibility(value: Boolean?) {
        isVisible = value ?: false
    }
}