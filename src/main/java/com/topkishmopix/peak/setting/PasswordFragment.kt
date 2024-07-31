package com.topkishmopix.peak.setting

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.database.base.ISettingsRepository
import com.fanap.corepos.database.service.model.SettingsNames
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.utils.Utils
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.databinding.FragmentPasswordBinding
import com.topkishmopix.peak.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import saman.zamani.persiandate.PersianDate
import javax.inject.Inject

@AndroidEntryPoint
class PasswordFragment : BaseFragment<FragmentPasswordBinding>() {


    @Inject
    lateinit var appContext: Context
    private var isTerminalPass: Boolean = true
    private lateinit var terminalPassword: String

    private val settingsRepository: ISettingsRepository by lazy {
        DependencyManager.provideSettingsRepository(
            appContext
        )
    }


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPasswordBinding.inflate(inflater,container,false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startTimer(20_000)
        isTerminalPass = requireArguments().getBoolean("IsTerminalPass")

        lifecycleScope.launch(Dispatchers.IO) {
            terminalPassword =
                settingsRepository.getValue(SettingsNames.Password.name)?.value ?: "1234"
            if (terminalPassword == "0000" && isTerminalPass)
                withContext(Dispatchers.Main) {
                    navigate(
                     this@PasswordFragment,
                     requireArguments().getInt("Destination")
                    )
                }
        }

        onTimerFinish.observe(viewLifecycleOwner, {
            finish(this)
        })

        onTimerTick.observe(viewLifecycleOwner,{
            binding.timer.text = it
        })
        onBackPressed.observe(viewLifecycleOwner,{ finish(this) })

        if (!isTerminalPass) {
            binding.title.text = "لطفا رمز مدیر را وارد کنید"
            binding.edtPassword.hint = "رمز مدیر"
            val filterArray = arrayOfNulls<InputFilter>(1)
            filterArray[0] = LengthFilter(6)
            binding.edtPassword.filters = filterArray
        }

        binding.confirm.setOnClickListener {
            val destination : Int  = requireArguments().getInt("Destination",0)
            if (TextUtils.isEmpty(binding.edtPassword.text.toString()))
                Utils.makeSnack(binding.root, "لطفا رمز عبور را وارد کنید.", Snackbar.LENGTH_SHORT).show()
            else {
                if (isTerminalPass) {
                    if (binding.edtPassword.text.toString() == terminalPassword) {
                        navigate(
                         this,
                         destination
                        )
                    } else {
                        Utils.makeSnack(binding.root, "رمز عبور اشتباه است.", Snackbar.LENGTH_SHORT).show()

                    }

                } else {
//                    val serial: String = DeviceSDKManager.getSerialInterface()?.serial ?: "-------"
                    val serial: String = DeviceSDKManager.getSerialSmartPeak()?.serial ?: "-------"
                    Log.d("wrong","df")
                    val date = PersianDate()
                    val month = if (date.shMonth<10) "0${date.shMonth}" else date.shMonth.toString()
                    val day = if (date.shDay<10) "0${date.shDay}" else date.shDay.toString()
                    if (binding.edtPassword.text.toString() == month + day + serial.substring(serial.length - 2)

                    ) {
                        navigate(
                         this,
                         destination
                        )
                    } else {
                        Utils.makeSnack(binding.root, "رمز عبور اشتباه است.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

        }

        binding.cancel.setOnClickListener { finish(this) }
    }

}