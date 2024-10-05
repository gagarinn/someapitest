package net.someapitest.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.someapitest.exchange.databinding.FragmentExchangeBinding
import net.someapitest.exchange.events.ExchangeEvents
import net.someapitest.exchange.my.balance.MyBalanceAdapter
import javax.inject.Inject

@AndroidEntryPoint
class ExchangeFragment : Fragment() {

        @Inject
    lateinit var myBalanceAdapter: MyBalanceAdapter
    var binding: FragmentExchangeBinding? = null
    private val viewModel: ExchangeViewModel by viewModels()

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
                }
            }
        }
    }

    private fun updateDevicesWithLifecycleScope() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.getBalance()
            }
        }
    }
}