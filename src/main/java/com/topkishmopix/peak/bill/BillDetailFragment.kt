package com.topkishmopix.peak.bill

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
import com.fanap.corepos.receipt.enum.TransactionReceiptStatus
import com.fanap.corepos.utils.RialFormatter
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.aryan.AryanResponse
import com.fanap.corepos.utils.sayan.SayanUtils
import com.topkishmopix.peak.R
import com.topkishmopix.peak.bill.viewmodel.BillDetailViewModel
import com.topkishmopix.peak.databinding.FragmentBillDetailBinding
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.main.view.view.LoadingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.properties.Delegates

//const val EDIT_PREF_TEXT = " com.masa.aryan.bill"

@AndroidEntryPoint
class BillDetailFragment : BaseFragment<FragmentBillDetailBinding>() {

    @Inject
    lateinit var appContext: Context
/*
@Inject
lateinit var share:SharedPreferences*/

    private val viewModel : BillDetailViewModel by viewModels()
    @Inject
    lateinit var loading: LoadingFragment
    private val transactionManager: IIso by lazy { DependencyManager.provideIsoTransaction() }

    private val hsm: HSMInterface by lazy { DeviceSDKManager.getHSMInterface(appContext)!! }
    private lateinit var track2 : String
    private lateinit var billId : String
    private lateinit var payId : String
    private lateinit var amount : String
    private lateinit var adviceTransaction : Transaction
    private lateinit var reverseTransaction : Transaction
    private lateinit var transaction : Transaction
    private var uId by Delegates.notNull<Long>()

//    val uId =  share.getString(com.masa.aryan.bill.EDIT_PREF_TEXT,"")!!.toLong()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBillDetailBinding {
        return FragmentBillDetailBinding.inflate(inflater,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        startTimer()
        track2 = arguments?.getString("Track2","") ?: ""
        onTimerFinish.observe(viewLifecycleOwner,{
            navigate(
                this,
                R.id.action_billDetailFragment_to_swipeFragment
            )
        })
        onBackPressed.observe(viewLifecycleOwner,{
            navigate(
                this,
                R.id.action_billDetailFragment_to_swipeFragment
            )
        })
        onTouchListener.observe(viewLifecycleOwner,{ startTimer() })

        binding.back.setOnClickListener { navigate(
            this,
            R.id.action_billDetailFragment_to_swipeFragment
        ) }
        binding.cancel.setOnClickListener { navigate(
            this,
            R.id.action_billDetailFragment_to_swipeFragment
        ) }


        billId = requireArguments().getString("BillId") ?: ""
        payId = requireArguments().getString("PayId") ?: ""
        amount = requireArguments().getString("Amount") ?: ""
        uId = requireArguments().getLong("UID")

        val billType: String = billId.substring(billId.length - 2, billId.length - 1)
        binding.txtBillType.text = Utils.getBillName(billType)
        binding.txtBillId.text = billId
        binding.txtPayId.text = payId
        binding.txtAmount.text = RialFormatter.format(amount)

        binding.confirm.setOnClickListener {
            showPinPad()
        }
    }

    private fun showPinPad(){
        hsm.inputPin(track2,appContext)
        hsm.mutablePassword.observe(viewLifecycleOwner,{
            if (!it.equals("")) {
                doTransaction(it)
            } else {
                navigate(
                    this,
                    R.id.action_billDetailFragment_to_swipeFragment
                )
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

            val map = viewModel.makeTransaction(track2,pinBlock,amount,billId,payId)
            transaction = viewModel.insertTransaction(map)

            transaction.description = binding.txtBillType.text.toString()
            transaction.description2 = map[IsoFields.BillId]
            transaction.description3 = map[IsoFields.PayId]

          /*  try{
                if(viewModel.userRepository.getValue(uId)!!.isEnabled)
                    transaction.userId = viewModel.userRepository.getEnabledUser(true)!!.userId
            }catch(e:Exception){}*/
            updateUserId()

            viewModel.updateTransaction(transaction)

            val result = transactionManager.doTransaction(map)

            withContext(Dispatchers.Main) {
                if (result != null) {
                    val responseCode = result[IsoFields.Response] ?: ""
                    if (responseCode == "00") {
                        transaction.response = responseCode
                        transaction.status = TransactionStatus.StartSuccessPrint.name
                        transaction.rrn = result[IsoFields.Rrn] ?: ""

                      /*  try{
                            if(viewModel.userRepository.getValue(uId)!!.isEnabled)
                                transaction.userId = viewModel.userRepository.getEnabledUser(true)!!.userId
                        }catch(e:Exception){}*/

                        updateUserId()


                        viewModel.updateTransaction(transaction)
                        val receipt = ReceiptFactory.getReceiptBitmap(requireContext(), viewModel.makeReceipt(track2,transaction))
                        val printResult = DeviceSDKManager.getPrinterInterface(requireContext())?.printBitmap(receipt)
                        if (printResult == true){
                            transaction.status = TransactionStatus.ReceiptPrinted.name
                            viewModel.updateTransaction(transaction)
                        }

                        val adviceMap = viewModel.makeAdvice(transaction, track2)
                        adviceTransaction = viewModel.insertTransaction(adviceMap)
                        advice(adviceMap,result)
                    }else{
                        transaction.response = responseCode
                        transaction.rrn = result[IsoFields.Rrn] ?: ""
                        transaction.status = TransactionStatus.TransactionResUnpackedResponseError.name

                        updateUserId()

                        viewModel.updateTransaction(transaction)
                        val data = viewModel.makeReceipt(track2,transaction)
                        data[IsoFields.Status] = TransactionReceiptStatus.Fail.name
                        data[IsoFields.FailMessage] = AryanResponse.getResponse(responseCode)
                        val receipt = ReceiptFactory.getReceiptBitmap(requireContext(), data)
                        DeviceSDKManager.getPrinterInterface(requireContext())?.printBitmap(receipt)
                        navigate(
                            this@BillDetailFragment, R.id.action_billDetailFragment_to_failFragment,
                            bundleOf("Response" to responseCode)
                        )
                    }
                } else {
                    transaction.response = "-1"
                    transaction.status = TransactionStatus.TransactionSentTimeOut.name

                    updateUserId()

                    viewModel.updateTransaction(transaction)
                    val data = viewModel.makeReceipt(track2,transaction)
                    data[IsoFields.Status] = TransactionReceiptStatus.UnReceivedResponse.name
                    data[IsoFields.FailMessage] = "خطا در انجام تراکنش"
                    val receipt = ReceiptFactory.getReceiptBitmap(requireContext(), data)
                    val printResult = DeviceSDKManager.getPrinterInterface(requireContext())?.printBitmap(receipt)
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
    private fun advice(
        adviceMap: HashMap<IsoFields, String>,
        response: HashMap<IsoFields, String>
    ) {
        adviceRepeatCount++
        lifecycleScope.launch(Dispatchers.IO) {
            val result = transactionManager.doTransaction(adviceMap)

            withContext(Dispatchers.Main) {
                if (result != null) {
                    val responseCode = result[IsoFields.Response] ?: ""
                    if (SayanUtils.isSuccessfulResponseForConfirmAndReverse(responseCode)) {
                        adviceTransaction.response = responseCode
                        adviceTransaction.status = TransactionStatus.AdviceResUnpacked.name
                        transaction.status = TransactionStatus.AdviceResUnpacked.name

                        updateUserId()

                        viewModel.updateTransaction(adviceTransaction)
                        viewModel.updateTransaction(transaction)
                        navigate(
                            this@BillDetailFragment,
                            R.id.action_billDetailFragment_to_billSuccessFragment,
                            bundleOf("Result" to response, "Track2" to track2 ))
                    } else {
                        if (adviceRepeatCount < 5)
                            advice(adviceMap, response)
                        else {
                            adviceTransaction.response = responseCode
                            adviceTransaction.status =
                                TransactionStatus.AdviceResUnpackedResponseError.name

                            updateUserId()

                            viewModel.updateTransaction(adviceTransaction)
                            navigate(
                                this@BillDetailFragment,
                                R.id.action_billDetailFragment_to_billSuccessFragment,
                                bundleOf("Result" to response, "Track2" to track2 )
                            )
                        }
                    }
                } else {
                    if (adviceRepeatCount < 5)
                        advice(adviceMap, response)
                    else {
                        adviceTransaction.status = TransactionStatus.AdviceSentTimeOut.name
                       updateUserId()
                        viewModel.updateTransaction(adviceTransaction)
                        navigate(
                            this@BillDetailFragment,
                            R.id.action_billDetailFragment_to_billSuccessFragment,
                            bundleOf("Result" to response, "Track2" to track2 )
                        )
                    }
                }
            }
        }
    }
    private var reverseCount = 0
    private fun reverse(reverseMap: HashMap<IsoFields, String>) {
        reverseCount++
        lifecycleScope.launch(Dispatchers.IO) {
            val result = transactionManager.doTransaction(reverseMap)

            withContext(Dispatchers.Main) {
                if (result != null) {
                    val responseCode = result[IsoFields.Response] ?: ""
                    if (SayanUtils.isSuccessfulResponseForConfirmAndReverse(responseCode)) {
                        reverseTransaction.response = responseCode
                        reverseTransaction.status = TransactionStatus.ReverseResUnpacked.name
                        transaction.status = TransactionStatus.ReverseResUnpacked.name
                        viewModel.updateTransaction(reverseTransaction)
                        updateUserId()
                        viewModel.updateTransaction(transaction)
                        navigate(
                            this@BillDetailFragment,
                            R.id.action_billDetailFragment_to_failFragment,
                            bundleOf("Result" to "عدم دریافت پاسخ تراکنش")
                        )
                    } else {
                        if (reverseCount < 5)
                            reverse(reverseMap)
                        else {
                            reverseTransaction.response = responseCode
                            reverseTransaction.status =
                                TransactionStatus.ReverseResUnpackedResponseError.name
//                            updateUserId()
                            viewModel.updateTransaction(reverseTransaction)
                            navigate(
                                this@BillDetailFragment,
                                R.id.action_billDetailFragment_to_failFragment,
                                bundleOf("Result" to "عدم دریافت پاسخ تراکنش")
                            )
                        }
                    }
                } else {
                    if (reverseCount < 5)
                        reverse(reverseMap)
                    else {
                        reverseTransaction.status = TransactionStatus.ReverseSentTimeOut.name
                        updateUserId()
                        viewModel.updateTransaction(reverseTransaction)
                        navigate(
                            this@BillDetailFragment,
                            R.id.action_billDetailFragment_to_failFragment,
                            bundleOf("Result" to "عدم دریافت پاسخ تراکنش")
                        )
                    }
                }
            }
        }
    }

    fun updateUserId(){
lifecycleScope.launch {
 try{
                if(viewModel.userRepository.getValue(uId)!!.isEnabled)
                    transaction.userId = viewModel.userRepository.getEnabledUser(true)!!.userId
            }catch(e:Exception){}
}
    }

}