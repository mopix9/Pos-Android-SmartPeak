package com.topkishmopix.peak.main.view.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.device.beep.BeepInterface
import com.fanap.corepos.device.beep.BeepType
import com.fanap.corepos.utils.aryan.AryanResponse
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentFailBinding
import com.topkishmopix.peak.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class FailFragment : BaseFragment<FragmentFailBinding>() {
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFailBinding =
        FragmentFailBinding.inflate(inflater,container,false)

    @Inject
    lateinit var appContext : Context
    private val beep : BeepInterface? by lazy { DeviceSDKManager.getSmartPeakBeepInterface() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startTimer(10_000)

        onTimerFinish.observe(viewLifecycleOwner) { binding.back.callOnClick() }
        binding.cancel.setOnClickListener { binding.back.callOnClick() }
        binding.back.setOnClickListener { navigate(
         this,
         R.id.action_failFragment_to_swipeFragment
        ) }

        val result : String? = arguments?.getString("Result")
        val response : String? = arguments?.getString("Response")

        result?.let {
            binding.txtMessage.text = result
        }

        response?.let {
            binding.txtDetail.text = AryanResponse.getResponse(response)
        }

        beep?.beep(BeepType.BEEP_TYPE_ERROR)
    }

}