package com.topkishmopix.peak.ns.base

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.basewin.log.LogUtil
import com.basewin.utils.AppTools

object BaseService {
 var androidContext: Context? = null
 fun ShowPermissions(): Array<String>? {
  val packManager: PackageManager =
   com.topkishmopix.peak.ns.base.BaseService.androidContext!!.getPackageManager()
  val infos = packManager.getInstalledApplications(PackageManager.GET_ACTIVITIES)
  var info: ApplicationInfo? = null
  try {
   info = packManager.getApplicationInfo(
    com.topkishmopix.peak.ns.base.BaseService.androidContext!!.getPackageName(),
    PackageManager.GET_PERMISSIONS
   )
  } catch (e1: PackageManager.NameNotFoundException) {
   // TODO Auto-generated catch block
   e1.printStackTrace()
  }
  // LOG.D("appName--->" + packManager.getApplicationLabel(info) + "");
  try {
   val packInfo = packManager.getPackageInfo(
    info!!.packageName,
    PackageManager.GET_PERMISSIONS
   )
   // 获取该app的所有权限
   // int length = permissons.length;
   // for (int i = 0; i < length; i++) {
   // LOG.D(permissons[i]);
   // }
   return packInfo.requestedPermissions
  } catch (e: Exception) {
   // TODO Auto-generated catch block
   // e.printStackTrace();
  }
  return null
 }

 /**
  *
  * @param perStrings
  * @param perString
  * @return
  */
 fun ifInside(perStrings: Array<String>, perString: String): Boolean {
  for (i in perStrings.indices) {
   if (perStrings[i] == perString) {
    LogUtil.i(
     AppTools::class.java,
     "验证包含 $perString"
    )
    return true
   }
  }
  return false
 }

 @Throws(Exception::class)
 fun validatePermission(permission: String?) {
  val ret = PackageManager.PERMISSION_DENIED
  var valid = false
  try {
   // LOG.D("检查" + permission);
   val permiStrings: Array<String>? = com.topkishmopix.peak.ns.base.BaseService.ShowPermissions()
   valid = permiStrings?.let { permission?.let { it1 ->
    com.topkishmopix.peak.ns.base.BaseService.ifInside(it,
     it1
    )
   } } == true
   // ret = androidContext.checkPermission(permission,
   // Binder.getCallingPid(), Binder.getCallingUid());
   // LOG.D("检查" + permission + "完成 "+valid);
  } catch (e: Exception) {
   // LOG.D("检查" + permission + "异常");
   e.printStackTrace()
  }

  // if (ret != PackageManager.PERMISSION_GRANTED) {
  // LOG.D("检查" + permission + "不满足 "+ret);
  // throw new SecurityException(String.format("Permission denied,
  // requires %s permission.", permission));
  // }
  if (valid != true) {
   // LOG.D("检查" + permission + "不满足 "+ret);
   throw Exception(String.format("Permission denied, requires %s permission.", permission))
   // throw new SecurityException(String.format("Permission denied,
   // requires %s permission.", permission));
  }
  // LOG.D("检查" + permission + "完成");
 }

 /**
  * 握奇的W9110设备需要屏蔽第1，2套密钥体系
  * @throws Exception
  */
/* @Throws(Exception::class)
 fun validateW9110() {
  com.pos.sdkdemo.base.BaseService.validateDeviceModel("W9110")
 }*/

 @Throws(Exception::class)
 fun validateDeviceModel(model: String) {
  if (Build.MODEL == model) {
   throw Exception("Permission denied,please use pinpad version 3.0 interface")
  }
 }

 @Throws(Exception::class)
 fun validateNull(`object`: Any?, objName: String) {
  if (`object` == null) {
   throw Exception("参数" + objName + "不可为null")
  }
 }

 @Throws(Exception::class)
 fun validateMinNumber(src: Int, srcName: String, target: Int) {
  if (src < target) {
   throw Exception("参数$srcName[int]不可小于$target")
  }
 }

 @Throws(Exception::class)
 fun validateMinEqualNumber(src: Int, srcName: String, target: Int) {
  if (src <= target) {
   throw Exception("参数$srcName[int]不可小于等于$target")
  }
 }

 @Throws(Exception::class)
 fun validateMaxNumber(src: Int, srcName: String, target: Int) {
  if (src > target) {
   throw Exception("参数$srcName[int]不可大于$target")
  }
 }

 @Throws(Exception::class)
 fun validateMaxEqualNumber(src: Int, srcName: String, target: Int) {
  if (src >= target) {
   throw Exception("参数$srcName[int]不可大于等于$target")
  }
 }

 @Throws(Exception::class)
 fun validateMinNumber(src: Long, srcName: String, target: Long) {
  if (src < target) {
   throw Exception("参数$srcName[long]不可小于$target")
  }
 }

 @Throws(Exception::class)
 fun validateMinEqualNumber(src: Long, srcName: String, target: Long) {
  if (src <= target) {
   throw Exception("参数$srcName[long]不可小于等于$target")
  }
 }

 @Throws(Exception::class)
 fun validateMaxNumber(src: Long, srcName: String, target: Long) {
  if (src > target) {
   throw Exception("参数$srcName[long]不可大于$target")
  }
 }

 @Throws(Exception::class)
 fun validateMaxEqualNumber(src: Long, srcName: String, target: Long) {
  if (src >= target) {
   throw Exception("参数$srcName[long]不可大于等于$target")
  }
 }

 /**
  * 判断参数是否在范围之内
  *
  * @param src
  * @param srcName
  * @param target
  * @throws DeviceException
  */
 @Throws(Exception::class)
 fun validateInclude(src: Int, srcName: String, target: IntArray) {
  var valid = false
  for (i in target.indices) {
   if (src == target[i]) {
    valid = true
    break
   }
  }
  if (!valid) {
   throw Exception("参数$srcName[int]异常,不在正常范围!")
  }
 }

 /**
  * 判断参数是否不在范围之内
  *
  * @param src
  * @param srcName
  * @param target
  * @throws DeviceException
  */
 @Throws(Exception::class)
 fun validateExInclude(src: Int, srcName: String, target: IntArray) {
  var valid = false
  for (i in target.indices) {
   if (src == target[i]) {
    valid = true
    break
   }
  }
  if (valid) {
   throw Exception("参数$srcName[int]异常,不在正常范围!")
  }
 }

 /**
  * 判断设备是否处于打开状态，非打开的话打开设备，打开了抛出异常，适合open接口
  *
  * @param isopen
  * @throws DeviceException
  */
 @Throws(Exception::class)
 fun DeviceOpen(isopen: Boolean) {
  if (isopen) {
   throw Exception("设备已打开，操作有误!")
  }
 }

 /**
  * 判断设备是否处于打开状态，非打开状态抛出异常
  *
  * @param isopen
  * @throws DeviceException
  */
 @Throws(Exception::class)
 fun validateDeviceIsOpen(isopen: Boolean) {
  if (!isopen) {
   throw Exception("设备还未打开，操作有误!")
  }
 }

 /**
  * 判断设备是否处于异步调用状态，非异步调用状态抛出异常，适合cancelreques
  *
  * @param iswork
  * @throws DeviceException
  */
 @Throws(Exception::class)
 fun DeviceCancelRequest(iswork: Boolean) {
  if (!iswork) {
   throw Exception("设备无异步调用,操作有误!")
  }
 }

 /**
  * 判断设备是否处于异步调用状态，处于异步调用状态抛出异常，适合非cancelrequest接口调用判断
  *
  * @param iswork
  * @throws DeviceException
  */
 @Throws(Exception::class)
 fun validateDeviceIsRequstPending(iswork: Boolean) {
  if (iswork) {
   throw Exception("设备处于异步调用状态,操作有误!")
  }
 }
}