package com.topkishmopix.peak.main.view

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.basewin.log.LogUtil
import com.basewin.services.ServiceManager
import com.fanap.corepos.database.base.ISettingsRepository
import com.fanap.corepos.database.service.model.SettingsNames
import com.fanap.corepos.di.DependencyManager
import com.topkishmopix.peak.R
import com.topkishmopix.peak.utils.GlobalData
import com.topkishmopix.peak.utils.SSLInitiator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {


    @Inject
    lateinit var appContext: Context

    private val settingsRepository: ISettingsRepository by lazy {
        DependencyManager.provideSettingsRepository(appContext)
    }

    var onTouchEvent = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
     super.onCreate(savedInstanceState)
     setContentView(R.layout.activity_main)
//     ifEntransActivityExist = true
     @Suppress("DEPRECATION")
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.insetsController?.hide(WindowInsets.Type.statusBars())
     } else {
      window.setFlags(
       WindowManager.LayoutParams.FLAG_FULLSCREEN,
       WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
     }

     GlobalData.getInstance().init(this.applicationContext)
permission()


     //Initialize Connection address if enabled
     lifecycleScope.launch {
       val isSSLEnabled = settingsRepository.getValue(SettingsNames.SSL.name)?.value.toBoolean()
            SSLInitiator().init(isSSLEnabled,appContext)
      settingsRepository.getValue(SettingsNames.Ip.name)?.value?.let {
       DependencyManager.ip = it
      }
      settingsRepository.getValue(SettingsNames.Port.name)?.value?.let {
       DependencyManager.port = it.toInt()
      }
      settingsRepository.getValue(SettingsNames.Nii.name)?.value?.let {
       DependencyManager.nii = it
      }
     }
    }

     @AfterPermissionGranted(REQUEST_PERMISSION)
     private fun permission() {
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
      if (EasyPermissions.hasPermissions(this, *perms)) {
       //Toast.makeText(mActivity,"Already Permission",Toast.LENGTH_SHORT).show();
//       startDemo()
       ServiceManager.getInstence().init(this.applicationContext)
       LogUtil.openLog()

      } else {
       // Do not have permissions, request them now
       EasyPermissions.requestPermissions(
        PermissionRequest.Builder(this, REQUEST_PERMISSION, *perms)
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

       ServiceManager.getInstence().init(this.applicationContext)
      }
     }

    override fun onPermissionsGranted(requestCode: Int, @NonNull perms: List<String>) {
      Log.e("Granted", "onPermissionsGranted:$requestCode:$perms")
     }

  override   fun onPermissionsDenied(requestCode: Int, @NonNull perms: List<String>) {
      Log.e("Denied", "onPermissionsDenied:$requestCode:$perms")
      if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
       AppSettingsDialog.Builder(this)
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


     override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
      onTouchEvent.postValue(true)
      return super.dispatchTouchEvent(ev)

     }


}