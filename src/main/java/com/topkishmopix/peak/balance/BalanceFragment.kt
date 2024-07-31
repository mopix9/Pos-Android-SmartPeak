package com.topkishmopix.peak.balance

import android.app.Activity
import android.content.Context
import android.graphics.Color.rgb
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.database.service.model.TransactionStatus
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.device.hsm.HSMInterface
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.IIso
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.utils.aryan.AryanResponse
import com.topkishmopix.peak.R
import com.topkishmopix.peak.balance.viewmodel.BalanceViewModel
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.databinding.FragmentBalanceBinding
import com.topkishmopix.peak.main.view.view.LoadingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class BalanceFragment : BaseFragment<FragmentBalanceBinding>() {

    @Inject
    lateinit var appContext: Context
    private val viewModel : BalanceViewModel by viewModels()
    @Inject
    lateinit var loading: LoadingFragment
    private val transactionManager: IIso by lazy { DependencyManager.provideIsoTransaction() }
    private var str_display = ""
    private val mActivity: Activity? = null

    private val hsm: HSMInterface by lazy { DeviceSDKManager.getHSMSmartPeakInterface(appContext.applicationContext)!! }
    private lateinit var track2 : String

    override fun getViewBinding(

        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBalanceBinding =
        FragmentBalanceBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        track2 = arguments?.getString("Track2","") ?: ""
        startTimer(30_000)

        onTimerFinish.observe(viewLifecycleOwner) {
            navigate(
             this,
             R.id.action_balanceFragment_to_swipeFragment
            )
        }
        onBackPressed.observe(viewLifecycleOwner) {
            navigate(
             this,
             R.id.action_balanceFragment_to_swipeFragment
            )
        }

        binding.back.setOnClickListener { navigate(
         this,
         R.id.action_balanceFragment_to_swipeFragment
        ) }
        binding.cancel.setOnClickListener { navigate(
         this,
         R.id.action_balanceFragment_to_swipeFragment
        ) }
        showPinPad()
    }




    private fun showPinPad() {

//        Toast karmozd

        val toast = Toast.makeText(
            appContext,
            "کارمزد 1206 ریال",
            Toast.LENGTH_SHORT
        )
        toast.setGravity(Gravity.TOP, 0, 50)
        val toastLayout = toast.getView() as ViewGroup
        val toastTV = toastLayout.getChildAt(0) as TextView
        toastTV.textSize = 30F
        toastTV.typeface = Typeface.createFromAsset(appContext.assets, "fonts/iransansbold.ttf")
        toastTV.setTextColor(rgb(255, 0, 0))
        toast.show()
//        toast ended



    hsm.inputPin(track2,appContext)
        hsm.mutablePassword.observe(viewLifecycleOwner) {
            if (!it.equals("")) {
                Log.d("password",it)
                doTransaction(it)
            } else {

                navigate(
                 this,
                 R.id.action_balanceFragment_to_swipeFragment
                )
            }
        }
    }

    private fun doTransaction(pinBlock: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                stopTimer()
                loading.show(childFragmentManager, "")
                loading.isCancelable = false
            }

            val map = viewModel.makeTransaction(track2,pinBlock)
            Log.d("map",map.toString())
            val transaction = viewModel.insertTransaction(map)
            val result = transactionManager.doTransaction(map)
            Log.d("resultMap" , result.toString())

            withContext(Dispatchers.Main) {
                if (result != null) {
                    val responseCode = result[IsoFields.Response] ?: ""
                    val balance = result[IsoFields.Balance]
                    Log.d("balance",balance.toString())

                    if (responseCode == "00") {
                        transaction.response = responseCode
                        transaction.status = TransactionStatus.StartSuccessPrint.name
                        transaction.rrn = result[IsoFields.Rrn] ?: ""
                        transaction.description = result[IsoFields.IdentificationCode] ?: ""
                        viewModel.updateTransaction(transaction)
                        navigate(
                         this@BalanceFragment,
                         R.id.action_balanceFragment_to_balanceSuccessFragment,
                         bundleOf("Result" to result, "Track2" to track2)
                        )
                    }else{
                        transaction.response = responseCode
                        transaction.status = TransactionStatus.TransactionResUnpackedResponseError.name
                        viewModel.updateTransaction(transaction)
                        navigate(
                         this@BalanceFragment, R.id.action_balanceFragment_to_failFragment,
                         bundleOf("Result" to "خطا: $responseCode")
                        )
                    }
                } else {
                    transaction.response = "-1"
                    transaction.status = TransactionStatus.TransactionSentTimeOut.name
                    viewModel.updateTransaction(transaction)
                    navigate(
                     this@BalanceFragment, R.id.action_balanceFragment_to_failFragment,
                     bundleOf("Result" to "عدم دریافت پاسخ تراکنش")
                    )
                }
            }
        }
    }

}