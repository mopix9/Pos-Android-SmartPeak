package com.topkishmopix.peak.setting.shop

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.database.base.ISettingsRepository
import com.fanap.corepos.database.base.ITransactionRepository
import com.fanap.corepos.database.service.model.Settings
import com.fanap.corepos.database.service.model.SettingsNames
import com.fanap.corepos.database.service.model.TransactionStatus
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.device.serial.SerialInterface
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.IIso
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.aryan.AryanUtils
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.BuildConfig
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.databinding.FragmentGetInfoBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class GetInfoFragment : BaseFragment<FragmentGetInfoBinding>() {

    @Inject
    lateinit var appContext: Context
    private val transactionRepository: ITransactionRepository by lazy { DependencyManager.provideTransactionRepository(appContext.applicationContext) }
    private val transactionManager: IIso by lazy { DependencyManager.provideIsoTransaction() }
    private val settingsRepository: ISettingsRepository by lazy { DependencyManager.provideSettingsRepository(appContext.applicationContext) }
    private val serialInterface: SerialInterface by lazy { DeviceSDKManager.getSerialSmartPeak()!! }
    //    private val serialInterface: SerialInterface by lazy { DeviceSDKManager.getSerialInterface()!! }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentGetInfoBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.back.setOnClickListener {
            finish(this)
        }

        onBackPressed.observe(viewLifecycleOwner,{
            finish(this)
        })

        getPosInfo()

    }

    private fun getPosInfo(){
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                stopTimer()
            }
            val stanList = transactionRepository.getStanSet()
            val map = HashMap<IsoFields, String>()
            map.apply {
                put(IsoFields.Mti, "0100")
                put(IsoFields.ProcessCode, "930000")
                put(IsoFields.Stan, stanList[1].toString())
                put(IsoFields.TransactionTime, AryanUtils.getTime())
                put(IsoFields.TransactionDate, AryanUtils.getDate())
                put(IsoFields.NiiCode, DependencyManager.nii)
                put(IsoFields.Terminal, settingsRepository.getValue(SettingsNames.TerminalNo.name)?.value ?: "")
//                put(IsoFields.Serial, serialInterface.serial)
                put(IsoFields.Serial,"00001504P6000003690") //smartpeak p600 serial

                put(IsoFields.SoftwareVersion, BuildConfig.VERSION_CODE.toString())
                put(IsoFields.TerminalLanguageCode, "0")
                put(IsoFields.ConnectionType, "7")

                put(IsoFields.Status, TransactionStatus.GetInfoSent.name)

            }

            val transaction = transactionRepository.insert(map)

            val result = transactionManager.doTransaction(map)


            withContext(Dispatchers.Main) {
                startTimer()
                if (result != null) {
                    val responseCode = result[IsoFields.Response] ?: ""
                    transaction.response = responseCode
                    transaction.status = TransactionStatus.GetInfoResReceived.name
                    transactionRepository.updateTransaction(transaction)
                    if (responseCode == "00") {

                        settingsRepository.insert(Settings(SettingsNames.MerchantNo.name,result[IsoFields.Merchant]!!))
                        settingsRepository.insert(Settings(SettingsNames.MerchantName.name,result[IsoFields.MerchantName]?:""))
                        settingsRepository.insert(Settings(SettingsNames.Phone.name,result[IsoFields.MerchantPhone]?:""))
                        settingsRepository.insert(Settings(SettingsNames.Address.name,result[IsoFields.Address]?:""))

                        Utils.makeSnack(binding.root,"دریافت اطلاعات با موفقیت انجام شد.", Snackbar.LENGTH_LONG).show()

                    }else{
                        Utils.makeSnack(binding.root,"خطا: $responseCode", Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    Utils.makeSnack(binding.root, "عدم دریافت پاسخ", Snackbar.LENGTH_LONG).show()
                }

                finish(this@GetInfoFragment)
            }

        }
    }

}