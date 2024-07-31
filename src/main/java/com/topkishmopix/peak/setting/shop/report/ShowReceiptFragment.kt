package com.topkishmopix.peak.setting.shop.report

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.database.base.ITransactionRepository
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.device.print.PrinterInterface
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.receipt.ReceiptFactory
import com.fanap.corepos.utils.RialFormatter
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.aryan.AryanResponse
import com.fanap.corepos.utils.aryan.AryanUtils
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.databinding.FragmentShowReceiptBinding
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.setting.shop.report.viewmodel.ShowReceiptViewModel
import com.topkishmopix.peak.main.view.view.LoadingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShowReceiptFragment : BaseFragment<FragmentShowReceiptBinding>() {

    @Inject
    lateinit var loading: LoadingFragment

    private val viewModel : ShowReceiptViewModel by viewModels()

    @Inject
    lateinit var appContext: Context
    private val transactionRepository: ITransactionRepository by lazy {
        DependencyManager.provideTransactionRepository(
            appContext
        )
    }

    private var transaction : Transaction? = null

    private val printer: PrinterInterface? by lazy {
        DeviceSDKManager.getPrinterInterface(appContext)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentShowReceiptBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.init()

        lifecycleScope.launch {
            transaction = transactionRepository.getTransaction(requireArguments().getLong("ID"))
            if (transaction != null)
                showTransaction(transaction!!)
            else {
                Utils.makeSnack(binding.root, "تراکنشی یافت نشد!", Snackbar.LENGTH_SHORT).show()
                finish(this@ShowReceiptFragment)
            }
        }

        binding.btnPrintReceipt.setOnClickListener{
            val receipt = ReceiptFactory.getReceiptBitmap(requireContext(), viewModel.makeReceipt(transaction)!!)
            lifecycleScope.launch(Dispatchers.IO) {
                val printResult = printer?.printBitmap(receipt)
                if (printResult == true){ }
            }
        }

        binding.back.setOnClickListener {
            finish(this)
        }

        onBackPressed.observe(viewLifecycleOwner,{
            finish(this)
        })
    }

    private fun showTransaction(item: Transaction) {
        binding.txtBank.text = Utils.getBankName(item.card ?: "")
        binding.txtCard.text = item.card
        binding.txtDate.text = AryanUtils.getShamsiDateFromString(item.date?.take(4) ?: "")
        binding.txtTime.text = AryanUtils.getTimeFromString(item.date?.takeLast(6) ?: "")
        if (item.rrn.isNullOrEmpty())
            item.rrn = "-"
        binding.txtRrnValue.text = item.rrn.toString() + "/" + item.stan
        when (item.processCode) {
            "000000" -> drawBuy(item)
            "170000" -> drawBill(item)
            "180000" -> drawCharge(item)
            "220000" -> drawTopup(item)
        }
    }

    private fun drawBuy(item: Transaction) {
        binding.txtTitle.text = "خرید"
        binding.txtResponse.visibility = View.VISIBLE
        binding.txtResponse.text = AryanResponse.getResponse(item.response ?: "")
        binding.txtResponse.setTextColor(
            if (item.response.equals("00")
            ) Color.parseColor("#2E7D32") else Color.parseColor("#f20606")
        )
        if (item.response.equals("00")) {
            binding.row1.visibility = View.VISIBLE
            binding.row2.visibility = View.GONE
            binding.row3.visibility = View.GONE
            binding.row4.visibility = View.GONE
            binding.txtRow1.text = "مبلغ"
            binding.txtRow1Value.text = RialFormatter.format(item.amount ?: "") + "ریال"

        } else {
            binding.row1.visibility = View.GONE
            binding.row2.visibility = View.GONE
            binding.row3.visibility = View.GONE
            binding.row4.visibility = View.GONE
        }
    }

    private fun drawTopup(item: Transaction) {
        binding.txtResponse.visibility = View.VISIBLE
        binding.txtTitle.text = "شارژ مستقیم"
        binding.txtResponse.text = AryanResponse.getResponse(item.response ?: "")
        binding.txtResponse.setTextColor(
            if (item.response.equals("00")
            ) Color.parseColor("#2E7D32") else Color.parseColor("#f20606")
        )
        if (item.response.equals("00")) {
            binding.row1.visibility = View.VISIBLE
            binding.txtRow1.text = "مبلغ شارژ"
            binding.txtRow1Value.text = RialFormatter.format(item.amount ?: "") + "ریال"
            binding.row2.visibility = View.VISIBLE
            binding.txtRow2.text = item.description
            binding.txtRow2Value.text = item.description2
            binding.row3.visibility = View.GONE
            binding.row4.visibility = View.GONE
        } else {
            binding.row1.visibility = View.VISIBLE
            binding.txtRow1.text = item.description
            binding.txtRow1Value.text = item.description2
            binding.row2.visibility = View.GONE
            binding.row3.visibility = View.GONE
            binding.row4.visibility = View.GONE
        }
    }

    private fun drawCharge(item: Transaction) {
        binding.txtResponse.visibility = View.VISIBLE

        binding.txtTitle.text = "خرید شارژ " + item.description
        binding.txtResponse.text = AryanResponse.getResponse(item.response ?: "")
        binding.txtResponse.setTextColor(
            if (item.response.equals("00"))
                Color.parseColor("#2E7D32")
            else Color.parseColor("#f20606")
        )
        if (item.response.equals("00")) {
            binding.row1.visibility = View.VISIBLE
            binding.txtRow1.text = "مبلغ شارژ"
            binding.txtRow1Value.text = RialFormatter.format(item.amount ?: "") + "ریال"
            binding.row2.visibility = View.GONE
            binding.row3.visibility = View.GONE
            binding.row4.visibility = View.GONE

        } else {
            binding.row1.visibility = View.GONE
            binding.row2.visibility = View.GONE
            binding.row3.visibility = View.GONE
            binding.row4.visibility = View.GONE
        }
    }

    private fun drawBill(item: Transaction) {
        binding.txtResponse.visibility = View.VISIBLE
        binding.txtTitle.text = "پرداخت قبض"
        binding.txtResponse.text = AryanResponse.getResponse(item.response ?: "")
        binding.row2.visibility = View.VISIBLE
        binding.txtRow2.text = "سازمان قبض"
        binding.txtRow2Value.text = item.description
        binding.row3.visibility = View.VISIBLE
        binding.txtRow3.text = "شناسه قبض"
        binding.txtRow3Value.text = item.description2
        binding.row4.visibility = View.VISIBLE
        binding.txtRow4.text = "شناسه پرداخت"
        binding.txtRow4Value.text = item.description3

        binding.txtResponse.setTextColor(
            if (item.response.equals("00"))
                Color.parseColor("#2E7D32")
            else Color.parseColor("#f20606")
        )
        if (item.response.equals("00")) {
            binding.row1.visibility = View.VISIBLE
            binding.txtRow1.text = "مبلغ"
            binding.txtRow1Value.text = RialFormatter.format(item.amount ?: "") + "ریال"
        } else {
            binding.row1.visibility = View.GONE
        }
    }

}