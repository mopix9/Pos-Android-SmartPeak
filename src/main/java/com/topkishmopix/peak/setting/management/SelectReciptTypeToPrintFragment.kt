package com.topkishmopix.peak.setting.management
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentSelectReciptTypeToPrintBinding
import com.topkishmopix.peak.base.BaseFragment

class SelectReciptTypeToPrintFragment : BaseFragment<FragmentSelectReciptTypeToPrintBinding>() {

 override fun getViewBinding(
  inflater: LayoutInflater,
  container: ViewGroup?
 ) = FragmentSelectReciptTypeToPrintBinding.inflate(inflater, container, false)

 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)

  binding.reciptPrintSetting.setOnClickListener {
   navigate(
    this,
    R.id.action_selectReciptTypeToPrintFragment_to_reciptPrintSettingFragment
   )
  }

  binding.firstSecondRecipt.setOnClickListener {
   navigate(
    this,
    R.id.action_selectReciptTypeToPrintFragment_to_firstReciptAndSecondReciptFragment
   )
  }

  binding.back.setOnClickListener {

   finish(this@SelectReciptTypeToPrintFragment)
  }

  onBackPressed.observe(viewLifecycleOwner) {
   finish(this@SelectReciptTypeToPrintFragment)
  }
 }
}


