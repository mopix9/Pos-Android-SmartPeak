package com.topkishmopix.peak.setting.management

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fanap.corepos.utils.RialFormatter
import com.fanap.corepos.utils.Utils
import com.topkishmopix.peak.databinding.FragmentReciptPrintSettingBinding
import com.topkishmopix.peak.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val AMOUNT_PREF_KEY = " com.masa.aryan.amount"

@AndroidEntryPoint
class ReciptPrintSettingFragment : BaseFragment<FragmentReciptPrintSettingBinding>() {


 @Inject
 lateinit var appContext: Context

 @Inject
 lateinit var sharedpref: SharedPreferences



 override fun getViewBinding(
  inflater: LayoutInflater,
  container: ViewGroup?
 ) = FragmentReciptPrintSettingBinding.inflate(inflater, container, false)

 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)
 binding.amountRecipt.addTextChangedListener(RialFormatter(binding.amountRecipt))

  binding.amountRecipt.setText(sharedpref.getString(AMOUNT_PREF_KEY, ""))

  binding.sendamount.setOnClickListener {
   sharedpref.edit().apply {
    putString(AMOUNT_PREF_KEY, binding.amountRecipt.text.toString())
   }.apply()

    Utils.makeSnack(binding.root,"تنظیمات با موفقیت ذخیره شد",900).show()

    finish(this@ReciptPrintSettingFragment)
  }
  binding.exit.setOnClickListener {
   finish(this@ReciptPrintSettingFragment)
  }

  binding.back.setOnClickListener {
   finish(this@ReciptPrintSettingFragment)
  }

  onBackPressed.observe(viewLifecycleOwner) {
   finish(this@ReciptPrintSettingFragment)
  }
 }
}




