package com.topkishmopix.peak.base

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.topkishmopix.peak.R
import com.topkishmopix.peak.widget.ProcessDialog
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.InvocationTargetException


abstract class BaseActivity : Activity() {
    /**
     * clear logs
     */
    protected var processdialog: ProcessDialog? = null
    protected var inflater: LayoutInflater? = null
    protected var MAX_TRY_CNT = 100
   var contentVieww: View? = null
    private var sb = StringBuffer("")
    private var showlogs: TextView? = null
    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SHOWLOG -> {
                    sb.append(
                        """
                            ${msg.obj}
                            
                            """.trimIndent()
                    )
                    showlogs!!.text = sb.toString()
                }
                CLEARLOG -> {
                    sb = StringBuffer("")
                    showlogs!!.text = sb.toString()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState)
        if (null == contentVieww) {
            inflater = LayoutInflater.from(this)
            contentVieww = inflater!!.inflate(R.layout.activity_base, null)
            setContentView(contentVieww)
            val contentLayout = contentVieww!!.findViewById<View>(R.id.ContentLayout) as LinearLayout
            showlogs = contentVieww!!.findViewById<View>(R.id.logs) as TextView
            val subContent = onCreateView(inflater)
            if (null != subContent) {
                contentLayout.addView(
                    subContent,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
            onInitView()
            sb = StringBuffer("")
        }
    }

    /**
     * for subclass create layout
     *
     * @param inflater
     * @return
     */
    protected abstract fun onCreateView(inflater: LayoutInflater?): View?

    /**
     * for subclass init view
     */
    protected abstract fun onInitView()

    /**
     * show logs
     *
     * @param msg
     */
    fun LOGD(msg: String?) {
        val message = Message()
        message.what = SHOWLOG
        message.obj = msg
        handler.sendMessage(message)
    }

    /**
     * clear logs
     */
    protected fun CLearLog() {
        val message = Message()
        message.what = CLEARLOG
        handler.sendMessage(message)
    }

    /**
     * 隐藏输入法键盘
     * @param view
     */
    protected fun hideSoftKeyBoard(view: View?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        try {
            val cls = EditText::class.java
            val method = cls.getMethod(
                "setShowSoftInputOnFocus",
                Boolean::class.javaPrimitiveType
            ) // 4.0的是setShowSoftInputOnFocus，4.2的是setSoftInputShownOnFocus
            method.isAccessible = false
            method.invoke(view, false)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    /**
     * 显示输入法键盘
     * @param view
     */
    protected fun showSoftKeyBoard(view: View?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        try {
            val cls = EditText::class.java
            val method = cls.getMethod(
                "setShowSoftInputOnFocus",
                Boolean::class.javaPrimitiveType
            ) // 4.0的是setShowSoftInputOnFocus，4.2的是setSoftInputShownOnFocus
            method.isAccessible = true
            method.invoke(view, true)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    /**
     * optimize soft input
     * @param view
     */
    protected fun optimizSoftKeyBoard(view: View) {
        hideSoftKeyBoard(view)
        view.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            if (b) {
                showSoftKeyBoard(view)
            } else {
                hideSoftKeyBoard(view)
            }
        }
    }

    /**
     *
     */
    fun showProcessDialog(title: String?) {
        if (processdialog == null) processdialog = title?.let { ProcessDialog(this, it) } else {
            processdialog!!.freshTitle(title)
        }
    }

    /**
     *
     */
    fun dismissProcessDialog() {
        if (processdialog != null && processdialog!!.isShowing()) {
            processdialog!!.stopTimer()
            processdialog!!.dismiss()
            processdialog = null
        }
    }

    companion object {
        private const val TAG = "BaseActivity"

        /**
         * show logs
         */
        private const val SHOWLOG = 1
        private const val CLEARLOG = 2
    }
}
