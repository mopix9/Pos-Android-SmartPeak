package com.topkishmopix.peak.card

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.pos.sdk.cardreader.PosCardReaderManager
import com.pos.sdk.cardreader.PosMagCardReader
import com.pos.sdk.utils.PosUtils
import com.topkishmopix.peak.R
import com.topkishmopix.peak.base.BaseActivity

class MagCard : BaseActivity() {
 var cardReader: PosMagCardReader? = null
 var ret = 0
 private var start: Button? = null
 private var v: View? = null


 override fun onCreateView(inflater: LayoutInflater?): View? {
  v = inflater!!.inflate(R.layout.activity_card_mag, null, false)
  return v
 }

 override fun onInitView() {
  if (v != null) {
   start = v!!.findViewById<View>(R.id.identify_card) as Button
   start!!.setOnClickListener {
    Thread {
     CardTest() }.start() }
  }
  cardReader = PosCardReaderManager.getDefault(this@MagCard).magCardReader
  if (cardReader != null) {
   LOGD("****** Mag test******")
   val ret = cardReader!!.open()
   LOGD("open:: " + if (ret == 0) "ok" else "fail")
  } else {
   LOGD("Mag cardreader is not support!")
  }
 }

 private fun CardTest() {
  LOGD("start to detect")
  var cnt = 0
  var detected = false
  while (cnt++ < MAX_TRY_CNT) {
   if (cardReader!!.detect() == 0) {
    detected = true
    break
   }
   PosUtils.delayms(50)
  }
  LOGD("detect:: " + if (detected) "ok" else "fail")
  if (detected) {
   var stripDataBytes: ByteArray? = null
   for (i in PosMagCardReader.CARDREADER_TRACE_INDEX_1..PosMagCardReader.CARDREADER_TRACE_INDEX_3) {
    stripDataBytes = cardReader!!.getTraceData(i)
    if (stripDataBytes != null) {
     LOGD("getTraceData:: strip" + i + "'s data= " + String(stripDataBytes))
    }
   }
  }
 }

 override fun onDestroy() {
  // TODO Auto-generated method stub
  super.onDestroy()
  ret = cardReader!!.close()
  LOGD("close:: " + if (ret == 0) "ok" else "fail")
 }
}
