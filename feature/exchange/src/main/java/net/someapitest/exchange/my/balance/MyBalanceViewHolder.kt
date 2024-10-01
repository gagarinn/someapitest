package net.someapitest.exchange.my.balance

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import net.someapitest.exchange.databinding.ItemBalanceBinding

sealed class MyBalanceViewHolder (val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    class BalanceViewHolder (binding: ItemBalanceBinding) : MyBalanceViewHolder(binding)
}