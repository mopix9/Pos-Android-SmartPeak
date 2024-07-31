package com.topkishmopix.peak.widget

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import com.topkishmopix.peak.R
import com.topkishmopix.peak.base.BaseDialog
import java.util.*

/**
 * @ClassName: ProcessDialog
 * @Description: TODO
 * @author: liudeyu
 * @date: 2016年3月4日 下午8:04:04
 */
class ProcessDialog(@get:JvmName("getAdapterContext") private val context: Context, private val title: String) :
 BaseDialog(context, R.layout.process_dialog, Gravity.CENTER) {
 /**
  * 超时时间默认为60秒
  */
 private val timeout = 60
 private var count = 0
 private var loding: ImageView? = null
 private var tv_timer: TextView? = null
 private var tv_title: TextView? = null
 private var timer: Timer? = null
 var handler: Handler? = null
 private fun initView() {
  loding = findViewById(R.id.loding) as ImageView?
  val animator = loding!!.background as AnimationDrawable
  loding!!.setImageDrawable(null)
  animator.start()
  tv_timer = findViewById(R.id.tv_timer) as TextView?
  tv_title = findViewById(R.id.tv_title) as TextView?
  tv_title!!.text = title
  handler = object : Handler(context.mainLooper) {
   override fun handleMessage(msg: Message) {
    if (msg.what == SHOW_TIME) {
     if (tv_timer != null) {
      tv_timer!!.text = msg.obj.toString()
     }
    } else if (msg.what == CLEAR) {
     if (tv_timer != null) {
      tv_timer!!.text = ""
     }
    }
   }
  }
 }

 internal inner class timerTask : TimerTask() {
  override fun run() {
   // TODO Auto-generated method stub
   count++
   Log.d("processdialog", "显示超时计数器 = $count")
   if (count >= timeout) {
    stopTimer()
   }
   val msg = Message()
   msg.what = SHOW_TIME
   msg.obj = timeout - count
   handler!!.sendMessage(msg)
  }
 }

 fun startTimerTask() {
  timer = Timer()
  count = 0
  val task: timerTask = timerTask()
  timer!!.schedule(task, 0, (1 * 1000).toLong())
 }

 fun stopTimer() {
  if (null != timer) {
   timer!!.cancel()
   timer = null
   handler!!.sendEmptyMessage(CLEAR)
  }
 }

 fun freshTitle(title: String?) {
  tv_title!!.text = title
 }

 companion object {
  private const val SHOW_TIME = 0
  private const val CLEAR = 1
 }

 init {
  // TODO Auto-generated constructor stub
  initView()
  setCancelable(false)
  setCanceledOnTouchOutside(false)
 }
}
