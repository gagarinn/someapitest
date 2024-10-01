package net.someapitest.exchange.my.balance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.someapitest.domain.models.Amount
import net.someapitest.exchange.databinding.ItemBalanceBinding
import javax.inject.Inject

class MyBalanceAdapter @Inject constructor() : RecyclerView.Adapter<MyBalanceViewHolder>() {

    private val items: MutableList<Amount> = mutableListOf()

    fun update(newItems: List<Amount>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBalanceViewHolder {
        return MyBalanceViewHolder.BalanceViewHolder(
            ItemBalanceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyBalanceViewHolder, position: Int) {
        val viewModel = MyBalanceViewModel(items[position])
        holder.binding.setVariable(net.someapitest.exchange.BR.itemModel, viewModel)
        holder.binding.executePendingBindings()
    }

    override fun getItemCount() = items.size
}