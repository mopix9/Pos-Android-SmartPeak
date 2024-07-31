package com.topkishmopix.peak.setting.management.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fanap.corepos.database.base.ISettingsRepository
import com.fanap.corepos.database.service.model.SettingsNames
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.enum.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ManagementViewModel (application: Application) : AndroidViewModel(application) {

    private val settingsRepository: ISettingsRepository by lazy { DependencyManager.provideSettingsRepository(getApplication()) }


    suspend fun makeSettingsReceipt(): HashMap<IsoFields, Any> =
        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            HashMap<IsoFields, Any>().apply {
                put(IsoFields.Type, TransactionType.Settings.name)
                put(
                    IsoFields.MerchantName,
                    settingsRepository.getValue(SettingsNames.MerchantName.name)?.value ?: ""
                )
                put(
                    IsoFields.Merchant,
                    settingsRepository.getValue(SettingsNames.MerchantNo.name)?.value ?: ""
                )
                put(
                    IsoFields.Terminal,
                    settingsRepository.getValue(SettingsNames.TerminalNo.name)?.value ?: ""
                )
                put(
                    IsoFields.Model,
                    settingsRepository.getValue(SettingsNames.Model.name)?.value ?: ""
                )
                put(
                    IsoFields.SoftwareVersion,
                    settingsRepository.getValue(SettingsNames.SoftwareVersion.name)?.value ?: ""
                )
                put(
                    IsoFields.Serial,
                    settingsRepository.getValue(SettingsNames.Serial.name)?.value ?: ""
                )


                put(
                    IsoFields.Buffer1,
                    settingsRepository.getValue(SettingsNames.Ip.name)?.value ?: ""
                )
                put(
                    IsoFields.Buffer2,
                    settingsRepository.getValue(SettingsNames.Port.name)?.value ?: ""
                )
                put(
                    IsoFields.NiiCode,
                    settingsRepository.getValue(SettingsNames.Nii.name)?.value ?: ""
                )


            }
        }

}