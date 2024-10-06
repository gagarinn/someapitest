package net.someapitest.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.someapitest.domain.models.Transaction
import net.someapitest.exchange.databinding.FragmentExchangeBinding
import net.someapitest.exchange.events.ExchangeEvents
import net.someapitest.exchange.my.balance.MyBalanceAdapter
import javax.inject.Inject
import net.someapitest.ui.R as coreR

@AndroidEntryPoint
class ExchangeFragment : Fragment() {

    @Inject
    lateinit var myBalanceAdapter: MyBalanceAdapter
    var binding: FragmentExchangeBinding? = null
    private val viewModel: ExchangeViewModel by viewModels()
    private val shownDialogs = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exchange, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExchangeBinding.bind(requireView())

        binding?.let {
            it.lifecycleOwner = viewLifecycleOwner
            it.model = viewModel
            it.adapter = myBalanceAdapter
        }

        setUpViewModelActions()
        updateDevicesWithLifecycleScope()
    }

    private fun setUpViewModelActions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.exchangeAction.collect { event ->
                when (event) {
                    is ExchangeEvents.OnBalanceUpdated -> myBalanceAdapter.update(event.data)
                    is ExchangeEvents.OnSubmited -> showSuccessTransaction(event.transaction)
                    is ExchangeEvents.OnError -> showDialog(
                        getString(coreR.string.error_title),
                        event.message ?: getString(coreR.string.unknown_error)
                    )
                }
            }
        }
    }

    private fun showSuccessTransaction(transaction: Transaction) {
        val title = getString(coreR.string.successSubmit)
        val commission = transaction.commission?.run {
            getString(coreR.string.commission, (formattedValue + currency.name))
        } ?: ""
        val message = getString(coreR.string.converted, (transaction.from.formattedValue + transaction.from.currency.name), (transaction.to.formattedValue + transaction.to.currency.name) ) + commission
        showDialog(title, message) {
            viewModel.onSubmited()
        }
    }

    private fun updateDevicesWithLifecycleScope() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.getBalance()
            }
        }
    }

    private fun showDialog(title: String, message: String, action: (() -> Unit)? = null) {
        if (shownDialogs.contains(title)) {
            return
        }
        shownDialogs.add(title)

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext()).apply {
            setMessage(message)
            setTitle(title)
            setCancelable(false)
            setPositiveButton(getString(coreR.string.done)) { dialog, _ ->
                action?.invoke()
                dialog.dismiss()
            }
        }
        builder.setOnDismissListener {
            shownDialogs.remove(title)
        }
        builder.create().show()
    }
}