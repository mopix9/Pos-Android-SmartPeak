package com.topkishmopix.peak.charge

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
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
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.aryan.AryanResponse
import com.fanap.corepos.utils.sina.SinaUtils
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.R
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.databinding.FragmentChargeBinding
import com.topkishmopix.peak.main.view.view.LoadingFragment
import com.topkishmopix.peak.setting.EDIT_PREF_TEXT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ChargeFragment : BaseFragment<FragmentChargeBinding>() {

    private val viewModel : ChargeViewModel by viewModels()
    private val AMOUNT_REQUEST = "200"
    private  var topupAmount: String? = null
    private  var chargeCodeAmount: String? = null

    @Inject
    lateinit var sharecharge: SharedPreferences

    @Inject
    lateinit var appContext: Context
    @Inject
    lateinit var loading: LoadingFragment
    private val transactionManager: IIso by lazy { DependencyManager.provideIsoTransaction() }
    private val hsm: HSMInterface by lazy { DeviceSDKManager.getHSMSmartPeakInterface(appContext.applicationContext)!! }
    private lateinit var track2 : String
    private lateinit var map: HashMap<IsoFields,String>

    private lateinit var adviceTransaction : Transaction
    private lateinit var reverseTransaction : Transaction
    private lateinit var transaction : Transaction


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChargeBinding = FragmentChargeBinding.inflate(inflater,container,false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel



        startTimer()
        track2 = arguments?.getString("Track2","") ?: ""
        onTimerFinish.observe(viewLifecycleOwner,{
            navigate(
                this,
                R.id.action_chargeFragment_to_swipeFragment
            )
        })
        onBackPressed.observe(viewLifecycleOwner,{
            navigate(
                this,
                R.id.action_chargeFragment_to_swipeFragment
            )
        })
        onTouchListener.observe(viewLifecycleOwner,{ startTimer() })
        binding.edtPhone.addTextChangedListener { startTimer() }

        binding.back.setOnClickListener { navigate(
            this,
            R.id.action_chargeFragment_to_swipeFragment
        ) }
        binding.cancel.setOnClickListener { navigate(
            this,
            R.id.action_chargeFragment_to_swipeFragment
        ) }

        setFragmentResultListener(AMOUNT_REQUEST) { _, bundle ->
            val amount = bundle.getString("Amount")

            if (viewModel.isTopUp.get()) {
                binding.txtTopupAmount.text = "$amount   ریال"
                topupAmount = amount.toString()
            } else {
                binding.txtCodeAmount.text = "$amount   ریال"
                chargeCodeAmount = amount.toString()
            }
        }

        binding.amountCode.setOnClickListener { showAmountBottomSheet(true) }
        binding.amountTopup.setOnClickListener { showAmountBottomSheet(false) }

        binding.confirm.setOnClickListener {
            if (viewModel.isTopUp.get()) {
                if (viewModel.topupOperator!=null)
                    sendTopupRequest()
                else
                    Utils.makeSnack(binding.main, "لطفا اپراتور مورد نظر را انتخاب کنید", Snackbar.LENGTH_SHORT).show()
            }
            else {
                if (viewModel.chargeCodeOperator!=null)
                    sendChargeCodeRequest()
                else
                    Utils.makeSnack(binding.main, "لطفا اپراتور مورد نظر را انتخاب کنید", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendChargeCodeRequest() {
        if (chargeCodeAmount != null && chargeCodeAmount != "") {
            binding.subtitle.visibility = View.GONE
            hsm.inputPin(track2.split("=".toRegex()).toTypedArray()[0], requireContext())
            hsm.mutablePassword.observe(viewLifecycleOwner,{
                if (!it.equals("")) {
                    doTransaction(pinBlock = it,chargeCodeAmount!!)
                } else {
                    navigate(
                        this,
                        R.id.action_chargeFragment_to_swipeFragment
                    )
                }
            })
        } else Utils.makeSnack(binding.main, "لطفا مبلغ شارژ را انتخاب کنید", Snackbar.LENGTH_SHORT).show()
    }

    private fun sendTopupRequest() {
        if (viewModel.isMtn.get() || viewModel.isMci.get() || viewModel.isRightel.get()) {
            if (viewModel.phone.get()?.length == 11) {
                if (topupAmount?.isNotBlank() == true) {
                    binding.subtitle.visibility = View.GONE
                    hsm.inputPin(track2,appContext)
                    hsm.mutablePassword.observe(viewLifecycleOwner,{
                        if (!it.equals("")) {
                            doTransaction(pinBlock = it,topupAmount!!)
                        } else {
                            navigate(
                                this,
                                R.id.action_chargeFragment_to_swipeFragment
                            )
                        }
                    })
                } else Utils.makeSnack(binding.main, "لطفا مبلغ شارژ را انتخاب کنید", Snackbar.LENGTH_SHORT).show()
            } else {
                Utils.makeSnack(binding.main, "لطفا یک شماره 11 رقمی وارد کنید.", Snackbar.LENGTH_SHORT).show()
            }
        } else {
            Utils.makeSnack(binding.main, "لطفا یک شماره معتبر وارد کنید.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showAmountBottomSheet(isChargeCode: Boolean) {
        val bundle = Bundle()
        if (isChargeCode) {
            bundle.putBoolean("1000", viewModel.isMciChargeCode.get())
        } else bundle.putBoolean("1000", true)
       navigate(
           this,
           R.id.action_chargeFragment_to_chargeAmountFragment,
           bundle
       )
    }

    private fun doTransaction(pinBlock: String,amount : String) {
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                stopTimer()
                loading.show(childFragmentManager, "")
                loading.isCancelable = false
            }

            map = viewModel.makeTransaction(track2,pinBlock,amount)
            transaction = viewModel.insertTransaction(map)

            if (viewModel.isTopUp.get()){
                transaction.description = viewModel.topupOperator?.opr
                transaction.description2 = viewModel.phone.get()
            }else{
                transaction.description = viewModel.chargeCodeOperator?.opr
            }

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
                        viewModel.updateTransaction(transaction)
                        val printResult = prepareReceiptPrint(result,amount)
                        if (printResult){
                            transaction.status = TransactionStatus.ReceiptPrinted.name

                            updateUserId()

                            viewModel.updateTransaction(transaction)
                        }

                        val adviceMap = viewModel.makeAdvice(transaction, track2)
                        adviceTransaction = viewModel.insertTransaction(adviceMap)
                        advice(adviceMap,result)

                    }else{
                        transaction.response = responseCode
                        transaction.status = TransactionStatus.TransactionResUnpackedResponseError.name

                        updateUserId()

                        viewModel.updateTransaction(transaction)
                        prepareReceiptPrint(result, amount)
                        navigate(
                            this@ChargeFragment, R.id.action_chargeFragment_to_failFragment,
                            bundleOf("Response" to responseCode)
                        )
                    }
                } else {
                    transaction.response = "-1"
                    transaction.status = TransactionStatus.TransactionSentTimeOut.name
                    viewModel.updateTransaction(transaction)
                    val printResult = prepareReceiptPrint(result, amount)
                    if (printResult){
                        transaction.status = TransactionStatus.ReceiptPrinted.name

                        updateUserId()

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

                        updateUserId()

                        viewModel.updateTransaction(transaction)
                        val operator : String
                        var phone : String? = null
                        if (viewModel.isTopUp.get()) {
                            operator = viewModel.topupOperator?.name ?: ""
                            phone = viewModel.phone.get() ?: ""
                        }
                        else
                          operator = viewModel.chargeCodeOperator?.name ?: ""

                        navigate(
                            this@ChargeFragment,
                            R.id.action_chargeFragment_to_chargeSuccessFragment,
                            bundleOf("Result" to response, "Track2" to track2, "Operator" to operator, "Phone" to phone)
                        )
                    }else {
                        if (adviceRepeatCount < 5)
                            advice(adviceMap,response)
                        else {
                            adviceTransaction.response = responseCode
                            adviceTransaction.status =
                                TransactionStatus.AdviceResUnpackedResponseError.name

                            updateUserId()

                            viewModel.updateTransaction(adviceTransaction)
                            navigate(
                                this@ChargeFragment,
                                R.id.action_chargeFragment_to_chargeSuccessFragment,
                                bundleOf("Result" to response, "Track2" to track2)
                            )
                        }
                    }
                } else {
                    if (adviceRepeatCount < 5)
                        advice(adviceMap,response)
                    else {
                        adviceTransaction.status = TransactionStatus.AdviceSentTimeOut.name


                        updateUserId()

                        viewModel.updateTransaction(adviceTransaction)
                        navigate(
                            this@ChargeFragment,
                            R.id.action_chargeFragment_to_chargeSuccessFragment,
                            bundleOf("Result" to response, "Track2" to track2)
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
                        navigate(
                            this@ChargeFragment, R.id.action_chargeFragment_to_failFragment,
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
                            navigate(
                                this@ChargeFragment, R.id.action_chargeFragment_to_failFragment,
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
                        navigate(
                            this@ChargeFragment, R.id.action_chargeFragment_to_failFragment,
                            bundleOf("Result" to "عدم دریافت پاسخ تراکنش")
                        )
                    }
                }
            }
        }
    }

    private suspend fun prepareReceiptPrint(result: HashMap<IsoFields, String>?, amount: String) : Boolean {

        return lifecycleScope.async(Dispatchers.IO) {
            if (result != null) {
                val responseCode = result[IsoFields.Response] ?: ""
                if (responseCode == "00") {
                    val data = viewModel.makeReceipt(track2, result,amount)
                    var receipt : Bitmap
                    withContext(Dispatchers.Main){
                        receipt = ReceiptFactory.getReceiptBitmap(requireContext(), data)
                    }
                    DeviceSDKManager.getPrintSmatPeakInterface(requireContext())?.printBitmap(receipt) ?: false
                } else {
                    val data = viewModel.makeReceipt(track2, result, amount)
                    data[IsoFields.Status] = TransactionReceiptStatus.Fail.name
                    data[IsoFields.FailMessage] = AryanResponse.getResponse(responseCode)

                    var receipt : Bitmap
                    withContext(Dispatchers.Main){
                        receipt = ReceiptFactory.getReceiptBitmap(requireContext(), data)
                    }
                    DeviceSDKManager.getPrintSmatPeakInterface(requireContext())?.printBitmap(receipt) ?: false
                }
            } else {
                val data = viewModel.makeReceipt(track2, map, amount)
                data[IsoFields.Status] = TransactionReceiptStatus.UnReceivedResponse.name
                data[IsoFields.FailMessage] = "خطا در انجام تراکنش"
                var receipt : Bitmap
                withContext(Dispatchers.Main){
                    receipt = ReceiptFactory.getReceiptBitmap(requireContext(), data)
                }

                    DeviceSDKManager.getPrintSmatPeakInterface(requireContext())?.printBitmap(receipt)
                        ?: false
            }
        }.await()
    }
    fun updateUserId(){
        lifecycleScope.launch {
             try{
                            if(viewModel.userRepository.getValue(  sharecharge.getString(EDIT_PREF_TEXT,"")!!.toLong())!!.isEnabled)
                                transaction.userId = viewModel.userRepository.getEnabledUser(true)!!.userId
                        }catch(e:Exception){}
        }
    }
}