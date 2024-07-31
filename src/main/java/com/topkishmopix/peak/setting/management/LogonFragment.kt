package com.topkishmopix.peak.setting.management

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.fanap.corepos.device.date.DateTimeInterface
import com.fanap.corepos.device.hsm.HSMInterface
import com.fanap.corepos.device.serial.SerialInterface
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.IIso
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.aryan.AryanUtils
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.BuildConfig
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.databinding.FragmentLogonBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class LogonFragment : BaseFragment<FragmentLogonBinding>() {

    @Inject
    lateinit var appContext: Context
    private val transactionRepository: ITransactionRepository by lazy { DependencyManager.provideTransactionRepository(appContext.applicationContext) }
    private val transactionManager: IIso by lazy { DependencyManager.provideIsoTransaction() }
    private val settingsRepository: ISettingsRepository by lazy { DependencyManager.provideSettingsRepository(appContext.applicationContext) }
    private val hsm: HSMInterface by lazy { DeviceSDKManager.getHSMSmartPeakInterface(appContext.applicationContext)!! }
    private val dateTimeInterface: DateTimeInterface by lazy { DeviceSDKManager.getDateTimeSmartPeakInterface(appContext.applicationContext)!! }
    private val serialInterface: SerialInterface by lazy { DeviceSDKManager.getSerialSmartPeak()!! }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLogonBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.back.setOnClickListener {
            finish(this)
        }

        onBackPressed.observe(viewLifecycleOwner) {
            finish(this)
        }

        logon()

    }

    private fun logon(){
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                stopTimer()
            }
            val stanList = transactionRepository.getStanSet()
            val map = HashMap<IsoFields, String>()
            map.apply {
                put(IsoFields.Mti, "0800")
                put(IsoFields.ProcessCode, "920000")
                put(IsoFields.Stan, stanList[1].toString())
                put(IsoFields.TransactionTime, AryanUtils.getTime())
                put(IsoFields.TransactionDate, AryanUtils.getDate())
                put(IsoFields.NiiCode, DependencyManager.nii)
//                put(IsoFields.Serial, serialInterface.serial)
                put(IsoFields.Serial,"00001504P6000003690") //smartpeak p600 serial

                put(IsoFields.SoftwareVersion, BuildConfig.VERSION_CODE.toString())
                put(IsoFields.TerminalLanguageCode, "0")

                put(IsoFields.Status, TransactionStatus.LogonSent.name)
            }

            val transaction = transactionRepository.insert(map)

            val result = transactionManager.doTransaction(map)

         /*   Log.d("trans1",result!!.get(IsoFields.DataKey).toString())
            Log.d("trans2", result.get(IsoFields.MacKey).toString())
            Log.d("trans3", result.get(IsoFields.MainKey).toString())
            Log.d("trans4", result.get(IsoFields.PinKey).toString())*/

            withContext(Dispatchers.Main) {
                startTimer()
                if (result != null) {
                    val responseCode = result[IsoFields.Response] ?: ""
                    transaction.response = responseCode
                    transaction.status = TransactionStatus.LogonResReceived.name
                    transactionRepository.updateTransaction(transaction)
                    if (responseCode == "00") {

                        Log.e("response",responseCode)

                        settingsRepository.insert(Settings(SettingsNames.TerminalNo.name,result[IsoFields.Terminal]!!))

                        hsm.loadMasterKey("3272A9DB5E07D848337D00C275CCC58A")
                        hsm.loadMacKey(result[IsoFields.MacKey])
//                        Log.d("loadKey", "${hsm.loadMacKey(result[IsoFields.MacKey])}")
                        hsm.loadPinKey(result[IsoFields.PinKey])
//                        Log.d("loadKey2", hsm.loadPinKey(result[IsoFields.PinKey]).toString())
                        hsm.loadDataKey(result[IsoFields.DataKey])
//                        Log.d("loadKey3", hsm.loadDataKey(result[IsoFields.DataKey]).toString())

                        dateTimeInterface.setDateTime(result[IsoFields.TransactionDateTime])

                        Utils.makeSnack(binding.root,"دریافت کلید با موفقیت انجام شد.",
                            Snackbar.LENGTH_LONG).show()

                    }else{
                        Utils.makeSnack(binding.root,"خطا: $responseCode", Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    Utils.makeSnack(binding.root, "عدم دریافت پاسخ", Snackbar.LENGTH_LONG).show()
                }

                finish(this@LogonFragment)
            }

        }
    }

}