package com.topkishmopix.peak.charge

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.device.beep.BeepInterface
import com.fanap.corepos.device.beep.BeepType
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.utils.RialFormatter
import com.fanap.corepos.utils.Utils
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentChargeSuccessBinding
import com.topkishmopix.peak.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChargeSuccessFragment : BaseFragment<FragmentChargeSuccessBinding>() {

    @Inject
    lateinit var appContext : Context
    private val beep : BeepInterface? by lazy { DeviceSDKManager.getBeepInterface() }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChargeSuccessBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startTimer(10_000)

        onTimerFinish.observe(viewLifecycleOwner,{ navigate(
         this,
         R.id.action_chargeSuccessFragment_to_swipeFragment
        ) })
        binding.cancel.setOnClickListener { navigate(
         this,
         R.id.action_chargeSuccessFragment_to_swipeFragment
        ) }
        binding.back.setOnClickListener { navigate(
         this,
         R.id.action_chargeSuccessFragment_to_swipeFragment
        ) }

        val result : HashMap<IsoFields, String> = arguments?.get("Result") as HashMap<IsoFields, String>
        val track2 : String = arguments?.getString("Track2") ?: ""
        val operator : String = arguments?.getString("Operator") ?: ""
        val mobile : String? = arguments?.getString("Phone")

        binding.txtAmount.text = RialFormatter.format(Utils.removeZeros(result[IsoFields.Amount]?: ""))
        binding.txtOperator.text = operator
        binding.txtDatetime.text = result[IsoFields.TransactionTime] ?: "00:00"
        binding.txtBank.text = Utils.getBankName(track2)
        binding.txtCard.text = Utils.cardMask(track2)

        mobile?.let {
            binding.txtMobile.text = mobile
            binding.lnrMobile.visibility = View.VISIBLE
        }

        beep?.beep(BeepType.BEEP_TYPE_SUCCESS)
    }

}