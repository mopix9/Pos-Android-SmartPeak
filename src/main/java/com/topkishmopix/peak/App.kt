package com.topkishmopix.peak

import android.app.Application
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.di.IsoProtocol
import com.fanap.corepos.receipt.ReceiptFactory
import com.fanap.corepos.receipt.util.ReceiptProtocol
import com.topkishmopix.peak.utils.GlobalData
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App :  Application()  {

    override fun onCreate() {
     super.onCreate()

     DependencyManager.protocol = IsoProtocol.ARYAN

     ReceiptFactory.protocol = ReceiptProtocol.ARYAN



//     ServiceManager.getInstence().init(applicationContext)



     GlobalData.getInstance().init(applicationContext)
//     NetworkChecker.check(applicationContext)

    }
}