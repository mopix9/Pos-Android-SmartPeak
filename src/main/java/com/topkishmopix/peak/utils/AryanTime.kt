package com.topkishmopix.peak.utils

import androidx.lifecycle.MutableLiveData
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat

object AryanTime {

    var getTime : MutableLiveData<String> = MutableLiveData(getDateString())

    fun updateTime(){ getTime.value = getDateString() }

     private fun getDateString(): String {
        val date = PersianDate()
        val formatter = PersianDateFormat("j F Y")
        return formatter.format(date)
    }

}