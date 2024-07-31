package com.topkishmopix.peak

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.basewin.define.GlobalDef
import com.topkishmopix.peak.card.MagCard
import com.topkishmopix.peak.utils.GlobalData


class MenuActivity:BootClass() {
 private val bootClass: BootClass? = null
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_main)
GlobalData.ifEntransActivityExist = true
 }

 override fun onResume() {
  super.onResume()
  Log.e(GlobalDef.VERSION, "start demo over")
 }
/*
 fun enterTransModule(view: View?) {
  overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
  startActivity(Intent(this@MenuActivity, TransModuleEntranceActivity::class.java))
 }*/

 fun enterSpecialCardsModule(view: View?) {
  overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
  startActivity(Intent(this@MenuActivity, MagCard::class.java))
 }

/* fun enterSystemModule(view: View?) {
  overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
  startActivity(Intent(this@MenuActivity, SystemModuleEntranceActivity::class.java))
 }*/

/* fun buildProjects(view: View?) {
  overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
  startActivity(Intent(this@MenuActivity, GuiderActivity::class.java))
 }*/

  override fun onDestroy() {
  super.onDestroy()
  GlobalData.ifEntransActivityExist = false
 }
}
