package com.topkishmopix.peak.ns.base

import android.util.Log
import com.basewin.services.*


/**
 * 作者: wdh <br></br>
 * 内容摘要: <br></br>
 * 创建时间:  2016/6/12 10:00<br></br>
 * 描述: 测试sdk是否有问题的一个类 <br></br>
 */
object BaseSDK {
 private val TAG = BaseSDK::class.java.name
 var beeper: BeeperBinder? = null
 var scan: ScanBinder? = null
 var led: LEDBinder? = null
 var pboc: PBOCBinder? = null
 var pinpad: PinpadBinder? = null
 var printer: PrinterBinder? = null
 var deviceinfo: DeviceInfoBinder? = null

 fun init() {
  Log.e(TAG, "init")
  try {
   pboc = ServiceManager.getInstence().pboc
   pinpad = ServiceManager.getInstence().pinpad
   printer = ServiceManager.getInstence().printer
   beeper = ServiceManager.getInstence().beeper
   scan = ServiceManager.getInstence().scan
   led = ServiceManager.getInstence().led
   deviceinfo = ServiceManager.getInstence().deviceinfo
  } catch (e: Exception) {
   Log.e(TAG, "初始化 aidl api 错误 /n " + e.message.toString())
   e.printStackTrace()
  }
  if (scan == null) {
   Log.e(TAG, "初始化 aidl scan 错误 /n ")
  } else {
   Log.e(TAG, "初始化 aidl scan 成功 /n ")
  }
  Log.e(TAG, "初始化Aidl api 成功 ")
 }
}