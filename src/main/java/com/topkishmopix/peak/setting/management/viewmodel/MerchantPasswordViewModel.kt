package com.topkishmopix.peak.setting.management.viewmodel

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fanap.corepos.database.service.model.Settings
import com.fanap.corepos.database.service.model.SettingsNames
import com.topkishmopix.peak.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MerchantPasswordViewModel (application: Application) : BaseViewModel(application) {

    val password = ObservableField("")
    val repeatPassword = ObservableField("")

    val onError = MutableLiveData<String>()
    val onSuccess = MutableLiveData<Boolean>()

    fun setOnConfirmClicked() {
        if (password.get() != "") {
            if (password.get()?.length == 4) {
                if (repeatPassword.get() != "") {
                    if (password.get() == repeatPassword.get()) {
                        if (repeatPassword.get()?.length == 4) {
                            update()
                        } else onError.postValue("طول تکرار رمز باید 4 کاراکتر باشد.")
                    }else onError.postValue("رمز جدید و تکرار آن یکسان نیست.")
                } else onError.postValue("لطفا تکرار رمز را وارد نمائید.")
            } else onError.postValue("طول رمز باید 4 کاراکتر باشد.")
        } else onError.postValue("لطفا رمز را وارد نمائید.")
    }

    private fun update(){
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.insert(Settings(SettingsNames.Password.name,password.get()?:"1234"))
            onSuccess.postValue(true)
        }
    }
}