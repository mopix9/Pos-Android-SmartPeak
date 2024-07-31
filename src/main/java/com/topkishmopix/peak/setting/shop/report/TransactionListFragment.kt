package com.topkishmopix.peak.setting.shop.report

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.device.print.PrinterInterface
import com.fanap.corepos.receipt.ReceiptFactory
import com.fanap.corepos.receipt.enum.TransactionType
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.aryan.AryanUtils
import com.google.android.material.snackbar.Snackbar
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog
import com.mohamadamin.persianmaterialdatetimepicker.time.RadialPickerLayout
import com.mohamadamin.persianmaterialdatetimepicker.time.TimePickerDialog
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar
import com.topkishmopix.peak.databinding.FragmentTransactionListBinding
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.setting.shop.report.adapter.TransactionAdapter
import com.topkishmopix.peak.setting.shop.report.viewmodel.TransactionListViewModel
import com.topkishmopix.peak.main.view.view.LoadingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TransactionListFragment : BaseFragment<FragmentTransactionListBinding>(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {


    @Inject
    lateinit var loading : LoadingFragment
    @Inject
    lateinit var appContext : Context
    private val printer: PrinterInterface? by lazy { DeviceSDKManager.getPrinterInterface(appContext) }

    private var selectedDate = ""
    private var isStartClicked = false
    private var startDate = ""
    private  var endDate = ""

    private var counter = 0L
    private lateinit var adapter: TransactionAdapter
    private var isLoading = false
    private lateinit var transactionList: MutableList<Transaction>
    private lateinit var printList:MutableList<Transaction>
    private lateinit var lists: List<List<Transaction>>

    private val viewModel : TransactionListViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTransactionListBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.init()

        viewModel.type = when(requireArguments().getSerializable("TYPE") as TransactionType){
            TransactionType.Buy -> "000000"
            TransactionType.Bill -> "170000"
            TransactionType.Topup -> "220000"
            TransactionType.Voucher -> "180000"
            else -> ""
        }

        binding.txtTitle.text = viewModel.getTitle()

        val linearLayoutManager = LinearLayoutManager(context)
        binding.rec.layoutManager = linearLayoutManager
        transactionList = ArrayList()
        printList = ArrayList()
        adapter = TransactionAdapter(transactionList)
        binding.rec.adapter = adapter
        binding.rec.addOnScrollListener(object :
            PaginationScrollListener(linearLayoutManager) {
            override fun loadMoreItems() {
                if (!isLoading) {
                    getMoreItems(++counter)
                }
            }
        })

        binding.btnStartDate.setOnClickListener {
            isStartClicked = true
            showDatePicker()
        }

        binding.btnEndDate.setOnClickListener {
            isStartClicked = false
            showDatePicker()
        }

        binding.back.setOnClickListener {
            finish(this)
        }

        onBackPressed.observe(viewLifecycleOwner,{
            finish(this)
        })

        binding.btnPrintReceipt.setOnClickListener {
            if (printList.size > 0) {
                lists = Utils.listOfLists(printList, 4)!!
                print()
            } else Utils.makeSnack(
                binding.back,
                "تراکنشی برای پرینت موجود نیست!",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun print() = lifecycleScope.launch(Dispatchers.Main) {
        loading.show(childFragmentManager, "")
        loading.isCancelable = false
        var result = false

        withContext(Dispatchers.IO) {
            lists.forEachIndexed { index, list ->
                val bitmap : Bitmap?
                when {
                    lists.size == 1 -> {
                        withContext(Dispatchers.Main) {
                            bitmap = ReceiptFactory.getReceiptBitmap(requireContext(), viewModel.makeAllReceipt(list, binding.txtStartDate.text.toString(),binding.txtEndDate.text.toString()))
                        }
                        bitmap?.let {
                            result = printer?.printBitmap(it) ?: false
                        }
                    }
                    index == 0 -> {
                        withContext(Dispatchers.Main) {
                            bitmap = ReceiptFactory.getReceiptBitmap(requireContext(), viewModel.makeHeaderReceipt(list, binding.txtStartDate.text.toString(),binding.txtEndDate.text.toString()))
                        }
                        bitmap?.let {
                            result = printer?.printBitmap(it) ?: false
                        }
                    }
                    index == lists.size - 1 -> {
                        withContext(Dispatchers.Main) {
                            bitmap = ReceiptFactory.getReceiptBitmap(requireContext(), viewModel.makeFooterReceipt(list, printList.sumOf { it.amount?.toLong()?:0 }.toString(),printList.size.toString()))
                        }
                        bitmap?.let {
                            result = printer?.printBitmap(it) ?: false
                        }
                    }
                    else -> {
                        withContext(Dispatchers.Main) {
                            bitmap = ReceiptFactory.getReceiptBitmap(requireContext(), viewModel.makeBodyReceipt(list))
                        }
                        bitmap?.let {
                            result = printer?.printBitmap(it) ?: false
                        }
                    }
                }
            }
        }
        loading.dismiss()
        if (!result)
            Utils.makeSnack(binding.root, "عدم چاپ رسید!", Snackbar.LENGTH_SHORT).show()
    }

    private fun getMoreItems(offset: Long) = lifecycleScope.launch {
        isLoading = true

        val sDate = startDate.dropLast(4)
        val eDate = endDate.dropLast(4)

        val end = Utils.shamsiToMiladi(eDate.split('/')[0].toInt(), eDate.split('/')[1].toInt(), eDate.split('/')[2].toInt())+endDate.takeLast(4)
        val start = Utils.shamsiToMiladi(sDate.split('/')[0].toInt(), sDate.split('/')[1].toInt(), sDate.split('/')[2].toInt()) + startDate.takeLast(4)

        val transactions = viewModel.getSuccessTransactionsLazy(start, end, offset * 10)
        if (transactions?.isNotEmpty() == true) {
            transactionList.addAll(transactions)
            adapter.notifyDataSetChanged()
            isLoading = false
        }
    }

    override fun onTimeSet(view: RadialPickerLayout?, hourOfDay: Int, minute: Int) {
        val hour = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay.toString()
        val min = if (minute < 10) "0$minute" else minute.toString()

        if (isStartClicked) {
            binding.txtStartDate.text = "$selectedDate $hour:$min"
            startDate = selectedDate + hour + min
        } else {
            binding.txtEndDate.text = "$selectedDate $hour:$min"
            endDate = selectedDate + hour + min
        }

        if (startDate != "" && endDate != "") {
            if (endDate >= startDate) {
                loading.show(childFragmentManager, "")
                loading.isCancelable = false
                getTransactions()
            } else {
                Utils.makeSnack(binding.btnStartDate, "تاریخ شروع باید قبل تر از تاریخ پایان باشد.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val newMonth = monthOfYear+1
        val month = if (newMonth < 10) "0$newMonth" else newMonth.toString()
        val day = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()

        selectedDate = "$year/$month/$day"
        showTimePicker()
    }

    private fun showDatePicker() {
        val persianCalendar = PersianCalendar()
        val datePickerDialog = DatePickerDialog.newInstance(
            this,
            persianCalendar.persianYear,
            persianCalendar.persianMonth,
            persianCalendar.persianDay
        )
        datePickerDialog.show(childFragmentManager,"")
    }

    private fun showTimePicker() {
        val now = PersianCalendar()
        val tpd = TimePickerDialog.newInstance(
            this,
            now[PersianCalendar.HOUR_OF_DAY],
            now[PersianCalendar.MINUTE],
            true
        )
        tpd.show(childFragmentManager, "")
    }

    private fun getTransactions() = lifecycleScope.launch {

        if (startDate.isNotBlank() && endDate.isNotBlank()){

            val sDate = startDate.dropLast(4)
            val eDate = endDate.dropLast(4)

            val end = AryanUtils.shamsiToMiladi(eDate.split('/')[0].toInt(), eDate.split('/')[1].toInt(), eDate.split('/')[2].toInt())+endDate.takeLast(4)+"00"
            val start = AryanUtils.shamsiToMiladi(sDate.split('/')[0].toInt(), sDate.split('/')[1].toInt(), sDate.split('/')[2].toInt()) + startDate.takeLast(4)+"00"

            isLoading = true
            counter = 0L
            transactionList.clear()
            adapter.notifyDataSetChanged()
            val transactions = viewModel.getSuccessTransactionsLazy(start, end, counter)
            loading.dismiss()
            if (!transactions.isNullOrEmpty()) {
                transactionList.addAll(transactions)
                binding.txtEmpty.visibility = View.GONE
                adapter.notifyDataSetChanged()
                binding.rec.visibility = View.VISIBLE
                binding.rec.smoothScrollToPosition(0)
            } else {
                binding.txtEmpty.visibility = View.VISIBLE
                binding.rec.visibility = View.GONE
                binding.txtEmpty.text = "تراکنشی موجود نیست!"
            }
            isLoading = false
            val print = viewModel.getSuccessTransactions(start, end)
            printList.clear()
            if (!print.isNullOrEmpty())
                printList.addAll(print)
        }
    }

}