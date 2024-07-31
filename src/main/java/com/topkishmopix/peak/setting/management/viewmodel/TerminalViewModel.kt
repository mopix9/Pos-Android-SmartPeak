package com.topkishmopix.peak.setting.management.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fanap.corepos.database.service.model.Settings
import com.fanap.corepos.database.service.model.SettingsNames
import com.topkishmopix.peak.base.BaseViewModel
import kotlinx.coroutines.launch

class TerminalViewModel(application: Application) : BaseViewModel(application) {

    var onSuccess: MutableLiveData<Boolean> = MutableLiveData()
    var onError: MutableLiveData<String> = MutableLiveData()

    fun setOnConfirmClicked(){
        if (observableTerminal.get()?.isNotBlank() == true){
            if (observableMerchant.get()?.isNotBlank() == true){
                viewModelScope.launch {

                    settingsRepository.insert(Settings(SettingsNames.TerminalNo.name,observableTerminal.get()?:terminal))
                    settingsRepository.insert(Settings(SettingsNames.MerchantNo.name,observableMerchant.get()?:merchant))

                    onSuccess.postValue(true)
                }

            }else onError.postValue("لطفا شماره پذیرنده معتبر وارد نمائید!")
        }else onError.postValue("لطفا شماره ترمینال معتبر وارد نمائید!")
    }

}