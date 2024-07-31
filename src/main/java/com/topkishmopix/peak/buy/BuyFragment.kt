package com.topkishmopix.peak.buy

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.database.service.model.TransactionStatus
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.device.hsm.HSMInterface
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.IIso
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.ReceiptFactory
import com.fanap.corepos.utils.RialFormatter
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.sina.SinaUtils
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.R
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.buy.viewmodel.BuyViewModel
import com.topkishmopix.peak.databinding.FragmentBuyBinding
import com.topkishmopix.peak.main.view.view.LoadingFragment

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class BuyFragment : BaseFragment<FragmentBuyBinding>() {

 @Inject
 lateinit var appContext: Context
 private val viewModel : BuyViewModel by viewModels()
 @Inject
 lateinit var loading: LoadingFragment
 private val transactionManager: IIso by lazy { DependencyManager.provideIsoTransaction() }

 private val hsm: HSMInterface by lazy { DeviceSDKManager.getHSMSmartPeakInterface(appContext.applicationContext)!! }
 private lateinit var track2 : String
 private lateinit var adviceTransaction : Transaction
 private lateinit var reverseTransaction : Transaction
 private lateinit var transaction : Transaction

 override fun getViewBinding(
  inflater: LayoutInflater,
  container: ViewGroup?
 ): FragmentBuyBinding  = FragmentBuyBinding.inflate(inflater,container,false)

 @SuppressLint("ClickableViewAccessibility")
 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)
  binding.viewModel = viewModel

  track2 = arguments?.getString("Track2","") ?: ""
  startTimer()

  binding.edtAmount.addTextChangedListener(RialFormatter(binding.edtAmount))

  onTimerFinish.observe(viewLifecycleOwner) {
   navigate(this, R.id.action_buyFragment_to_swipeFragment)
  }
  onBackPressed.observe(viewLifecycleOwner) {
   navigate(this, R.id.action_buyFragment_to_swipeFragment)
  }
  onTouchListener.observe(viewLifecycleOwner) { startTimer() }

  binding.back.setOnClickListener { navigate(this,R.id.action_buyFragment_to_swipeFragment) }
  binding.cancel.setOnClickListener { navigate(this,R.id.action_buyFragment_to_swipeFragment) }

  viewModel.onConfirmClicked.observe(viewLifecycleOwner,{
   showPinPad()
  })

  viewModel.onError.observe(viewLifecycleOwner,{
   Utils.makeSnack(binding.root,it, Snackbar.LENGTH_LONG).show()
  })
 }

 private fun showPinPad(){

  hsm.inputPin(track2,appContext)
  hsm.mutablePassword.observe(viewLifecycleOwner,{
   if (!it.equals("")) {
    doTransaction(it)
   } else {
    navigate(this,R.id.action_buyFragment_to_swipeFragment)
   }
  })
 }

 private fun doTransaction(pinBlock: String) {
  lifecycleScope.launch(Dispatchers.IO) {
   withContext(Dispatchers.Main) {
    stopTimer()
    loading.show(childFragmentManager, "")
    loading.isCancelable = false
   }

   val map = viewModel.makeTransaction(track2,pinBlock)
   transaction = viewModel.insertTransaction(map)
   val result = transactionManager.doTransaction(map)

   withContext(Dispatchers.Main) {
    if (result != null) {

     val responseCode = result[IsoFields.Response] ?: ""
     transaction.response = responseCode
     transaction.rrn = result[IsoFields.Rrn] ?: ""

     if (responseCode == "00") {
      transaction.status = TransactionStatus.StartSuccessPrint.name
      viewModel.updateTransaction(transaction)
      val receipt = ReceiptFactory.getReceiptBitmap(requireContext(), viewModel.makeReceipt(track2, transaction))
      val printResult = DeviceSDKManager.getPrinterInterface(requireContext())?.printBitmap(receipt)
      if (printResult == true){
       transaction.status = TransactionStatus.ReceiptPrinted.name
       viewModel.updateTransaction(transaction)
      }

      val adviceMap = viewModel.makeAdvice(transaction, track2)
      adviceTransaction = viewModel.insertTransaction(adviceMap)
      advice(adviceMap,result)

     }else{
      transaction.status = TransactionStatus.TransactionResUnpackedResponseError.name
      viewModel.updateTransaction(transaction)
      val receipt = ReceiptFactory.getReceiptBitmap(requireContext(), viewModel.makeFailReceipt(track2, transaction))
      DeviceSDKManager.getPrintSmatPeakInterface(appContext.applicationContext)?.printBitmap(receipt)
      navigate(this@BuyFragment, R.id.action_buyFragment_to_failFragment,
       bundleOf("Response" to responseCode)
      )
     }
    } else {
     transaction.response = "-1"
     transaction.status = TransactionStatus.TransactionSentTimeOut.name
     viewModel.updateTransaction(transaction)
     val receipt = ReceiptFactory.getReceiptBitmap(requireContext(), viewModel.makeNullResponseReceipt(track2, transaction))
     val printResult = DeviceSDKManager.getPrintSmatPeakInterface(appContext.applicationContext)?.printBitmap(receipt)
     if (printResult == true){
      transaction.status = TransactionStatus.ReceiptPrinted.name
      viewModel.updateTransaction(transaction)
     }

     val reverseMap = viewModel.makeReverse(transaction)
     reverseTransaction = viewModel.insertTransaction(reverseMap)
     reverse(reverseMap)
    }
   }
  }
 }

 private var adviceRepeatCount = 0
 private fun advice(adviceMap: HashMap<IsoFields, String>, response : HashMap<IsoFields, String>) {
  adviceRepeatCount ++
  lifecycleScope.launch(Dispatchers.IO) {
   val result = transactionManager.doTransaction(adviceMap)
   withContext(Dispatchers.Main) {
    if (result != null) {
     val responseCode = result[IsoFields.Response] ?: ""
     if (SinaUtils.isSuccessfulResponseForConfirmAndReverse(responseCode)) {
      adviceTransaction.response = responseCode
      adviceTransaction.status = TransactionStatus.AdviceResUnpacked.name
      transaction.status = TransactionStatus.AdviceResUnpacked.name
      viewModel.updateTransaction(adviceTransaction)
      viewModel.updateTransaction(transaction)
      navigate(
       this@BuyFragment, R.id.action_buyFragment_to_buySuccessFragment,
       bundleOf("Result" to response, "Track2" to track2 , "Amount" to transaction.amount)
      )
     } else {
      if (adviceRepeatCount < 5)
       advice(adviceMap,response)
      else {
       adviceTransaction.response = responseCode
       adviceTransaction.status = TransactionStatus.AdviceResUnpackedResponseError.name
       viewModel.updateTransaction(adviceTransaction)
       navigate(
        this@BuyFragment, R.id.action_buyFragment_to_buySuccessFragment,
        bundleOf("Result" to response, "Track2" to track2, "Amount" to transaction.amount)
       )
      }
     }
    } else {
     if (adviceRepeatCount < 5)
      advice(adviceMap,response)
     else {
      adviceTransaction.status = TransactionStatus.AdviceSentTimeOut.name
      viewModel.updateTransaction(adviceTransaction)
      navigate(
       this@BuyFragment, R.id.action_buyFragment_to_buySuccessFragment,
       bundleOf("Result" to response, "Track2" to track2, "Amount" to transaction.amount)
      )
     }
    }
   }
  }
 }

 private var reverseCount = 0
 private fun reverse(reverseMap: HashMap<IsoFields, String>) {
  reverseCount ++
  lifecycleScope.launch(Dispatchers.IO) {
   val result = transactionManager.doTransaction(reverseMap)
   withContext(Dispatchers.Main) {
    if (result != null) {
     val responseCode = result[IsoFields.Response] ?: ""
     if (SinaUtils.isSuccessfulResponseForConfirmAndReverse(responseCode)) {
      reverseTransaction.response = responseCode
      reverseTransaction.status = TransactionStatus.ReverseResUnpacked.name
      transaction.status = TransactionStatus.ReverseResUnpacked.name
      viewModel.updateTransaction(reverseTransaction)
      viewModel.updateTransaction(transaction)
      navigate(this@BuyFragment, R.id.action_buyFragment_to_failFragment,
       bundleOf("Result" to "عدم دریافت پاسخ تراکنش")
      )
     }else {
      if (reverseCount < 5)
       reverse(reverseMap)
      else {
       reverseTransaction.response = responseCode
       reverseTransaction.status =
        TransactionStatus.ReverseResUnpackedResponseError.name
       viewModel.updateTransaction(reverseTransaction)
       navigate(this@BuyFragment, R.id.action_buyFragment_to_failFragment,
        bundleOf("Result" to "عدم دریافت پاسخ تراکنش")
       )
      }
     }
    } else {
     if (reverseCount < 5)
      reverse(reverseMap)
     else {
      reverseTransaction.status = TransactionStatus.ReverseSentTimeOut.name
      viewModel.updateTransaction(reverseTransaction)
      navigate(this@BuyFragment, R.id.action_buyFragment_to_failFragment,
       bundleOf("Result" to "عدم دریافت پاسخ تراکنش")
      )
     }
    }
   }
  }
 }

}