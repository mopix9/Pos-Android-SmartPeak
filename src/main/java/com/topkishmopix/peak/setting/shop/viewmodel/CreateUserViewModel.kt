package com.topkishmopix.peak.setting.shop.viewmodel

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fanap.corepos.database.service.model.User
import com.topkishmopix.peak.base.BaseViewModel
import kotlinx.coroutines.launch

class CreateUserViewModel(application: Application): BaseViewModel(application) {

 val code = ObservableField("")
 val userName = ObservableField("")
 var onConfirmClicked: MutableLiveData<Boolean> = MutableLiveData()
// val codeRequest = ObservableField("")

 val onError = MutableLiveData<String>()
 val onSuccess = MutableLiveData<Boolean>()

 fun setOnConfirmClicked() {

   if (userName.get()?.isNotBlank() == true){
    if (code.get()?.isNotBlank() == true){

      onConfirmClicked.postValue(true)
      viewModelScope.launch {

       userRepository.insert(
        User(
         userName.get()!!.toString(),
         code.get()!!.toLong(),
         "",
         "",
         false,
         false
        )
       )

       onSuccess.postValue(true)
      }

    }else onError.postValue("لطفا کد را وارد نمائید!")
   }else onError.postValue("لطفا نام کاربری را وارد نمائید")

 }

}
