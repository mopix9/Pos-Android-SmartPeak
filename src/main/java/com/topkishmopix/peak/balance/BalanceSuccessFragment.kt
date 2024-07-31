package com.topkishmopix.peak.balance

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.device.beep.BeepInterface
import com.fanap.corepos.device.beep.BeepType
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.ReceiptFactory
import com.fanap.corepos.utils.RialFormatter
import com.fanap.corepos.utils.Utils
import com.topkishmopix.peak.R
import com.topkishmopix.peak.balance.viewmodel.BalanceViewModel
import com.topkishmopix.peak.databinding.FragmentBalanceSucessBinding
import com.topkishmopix.peak.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class BalanceSuccessFragment : BaseFragment<FragmentBalanceSucessBinding>() {

    private val viewModel : BalanceViewModel by viewModels()
    @Inject
    lateinit var appContext : Context
    private val beep : BeepInterface? by lazy { DeviceSDKManager.getBeepInterface() }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBalanceSucessBinding {
        return FragmentBalanceSucessBinding.inflate(inflater,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.init()

        startTimer(10_000)

        onTimerFinish.observe(viewLifecycleOwner,{
            navigate(
             this,
             R.id.action_balanceSuccessFragment_to_swipeFragment
            )
        })
        binding.btnCancel.setOnClickListener {
            navigate(
             this,
             R.id.action_balanceSuccessFragment_to_swipeFragment
            )
        }
        binding.back.setOnClickListener {
            navigate(
             this,
             R.id.action_balanceSuccessFragment_to_swipeFragment
            )
        }

        val result : HashMap<IsoFields, String> = arguments?.get("Result") as HashMap<IsoFields, String>
        val track2 : String = (arguments?.get("Track2") ?: "") as String

        var balance = ""
        result[IsoFields.Balance]?.let{
            balance = Utils.removeZeros(it.take(12))
        }

        binding.txtBalance.text = RialFormatter.format(balance)
        binding.txtDatetime.text = result[IsoFields.TransactionTime] ?: "00:00"
        binding.txtBankName.text = Utils.getBankName(track2)
        binding.txtCard.text = Utils.cardMask(track2)

        binding.btnPrint.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                val receipt = ReceiptFactory.getReceiptBitmap(requireContext(), viewModel.makeReceipt(track2, result))
                withContext(Dispatchers.IO){
                    DeviceSDKManager.getPrintSmatPeakInterface(requireContext())?.printBitmap(receipt)
                }
            }
        }

        beep?.beep(BeepType.BEEP_TYPE_SUCCESS)
    }
}