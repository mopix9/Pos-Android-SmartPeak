package com.topkishmopix.peak

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.annotation.NonNull
import com.basewin.database.DataBaseManager
import com.basewin.define.GlobalDef
import com.basewin.log.LogUtil
import com.basewin.services.ServiceManager
import com.topkishmopix.peak.utils.GlobalData

import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import pub.devrel.easypermissions.PermissionRequest


 open   class BootClass :  PermissionCallbacks , Activity() {
  open var mActivity: Activity? = null
 open var runnable: Runnable? = null
 override fun onResume() {
  super.onResume()
  mActivity = this
//
  start()
 }

 open fun start() {
  if (runnable == null) {
   runnable = Runnable {
    Log.e(GlobalDef.VERSION, "runable start")
    /**
     * init database
     */
    /**
     * init database
     */
    /**
     * init database
     */
    /**
     * init database
     */
    DataBaseManager.getInstance().init(mActivity!!.applicationContext)


//    DaoManager.init(mActivity!!.applicationContext)
    /**
     * init the GlobalData cashe
     */
    /**
     * init the GlobalData cashe
     */
    /**
     * init the GlobalData cashe
     */
    /**
     * init the GlobalData cashe
     */
    GlobalData.getInstance().init(mActivity!!.applicationContext)
    permission()
   }
   Thread(runnable).start()
  }
 }

 @SuppressLint("NewApi")
 private fun startDemo() {
  Log.e(GlobalDef.VERSION, "Start service init")
  ServiceManager.getInstence().init(mActivity!!.applicationContext)
  Log.e(GlobalDef.VERSION, "Start service stop")
  LogUtil.openLog()
 }

 @AfterPermissionGranted(REQUEST_PERMISSION)
open  fun permission() {
  val perms = arrayOf(
   Manifest.permission.WRITE_EXTERNAL_STORAGE,
   Manifest.permission.READ_EXTERNAL_STORAGE,
   Manifest.permission.ACCESS_FINE_LOCATION,
   Manifest.permission.SEND_SMS,
   "com.pos.permission.SECURITY",
   "com.pos.permission.ACCESSORY_DATETIME",
   "com.pos.permission.ACCESSORY_LED",
   "com.pos.permission.ACCESSORY_BEEP",
   "com.pos.permission.ACCESSORY_RFREGISTER",
   "com.pos.permission.CARD_READER_ICC",
   "com.pos.permission.CARD_READER_PICC",
   "com.pos.permission.CARD_READER_MAG",
   "com.pos.permission.COMMUNICATION",
   "com.pos.permission.PRINTER",
   "com.pos.permission.ACCESSORY_RFREGISTER",
   "com.pos.permission.EMVCORE"
  )
  if (EasyPermissions.hasPermissions(mActivity!!, *perms)) {
   //Toast.makeText(mActivity,"Already Permission",Toast.LENGTH_SHORT).show();
   startDemo()
  } else {
   // Do not have permissions, request them now
   EasyPermissions.requestPermissions(
    PermissionRequest.Builder(mActivity!!, REQUEST_PERMISSION, *perms)
     .setRationale("Dear users\n need to apply for storage Permissions for\n your better use of this application")
     .setNegativeButtonText("NO")
     .setPositiveButtonText("YES")
     .build()
   )
  }
 }

 override fun onRequestPermissionsResult(
  requestCode: Int,
  @NonNull permissions: Array<String>,
  @NonNull grantResults: IntArray
 ) {
  super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  Log.e("Granted", "onRequestPermissionsResult:$requestCode")
  if (requestCode == 1) {
   startDemo()
  }
 }

 override fun onPermissionsGranted(requestCode: Int, @NonNull perms: List<String>) {
  Log.e("Granted", "onPermissionsGranted:$requestCode:$perms")
 }

 override fun onPermissionsDenied(requestCode: Int, @NonNull perms: List<String>) {
  Log.e("Denied", "onPermissionsDenied:$requestCode:$perms")
  if (EasyPermissions.somePermissionPermanentlyDenied(mActivity!!, perms)) {
   AppSettingsDialog.Builder(mActivity!!)
    .setTitle("温馨提示")
    .setRationale("尊敬的用户为了您能更好的使用本应用需要申请存储权限")
    .setNegativeButton("拒绝")
    .setPositiveButton("去设置")
    .setRequestCode(0x001)
    .build()
    .show()
  }
 }

 companion object {
  const val REQUEST_PERMISSION = 0x01
 }
}
