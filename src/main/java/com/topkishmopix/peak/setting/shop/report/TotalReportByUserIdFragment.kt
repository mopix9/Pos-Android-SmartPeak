package com.topkishmopix.peak.setting.shop.report

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.database.base.ITransactionRepository
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.device.print.PrinterInterface
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.ReceiptFactory
import com.fanap.corepos.receipt.enum.TransactionType
import com.fanap.corepos.utils.RialFormatter
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.aryan.AryanUtils
import com.google.android.material.snackbar.Snackbar
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog
import com.mohamadamin.persianmaterialdatetimepicker.time.RadialPickerLayout
import com.mohamadamin.persianmaterialdatetimepicker.time.TimePickerDialog
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar
import com.topkishmopix.peak.databinding.FragmentTotalReportByUserIdBinding
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.setting.shop.report.viewmodel.TotalReportViewModel
import com.topkishmopix.peak.main.view.view.LoadingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TotalReportByUserIdFragment : BaseFragment<FragmentTotalReportByUserIdBinding>(),
 TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

 private var selectedDate = ""
 private var isStartClicked = false
 private var startDate = ""
 private var endDate = ""
 private val viewModel: TotalReportViewModel by viewModels()

 @Inject
 lateinit var loading: LoadingFragment

 @Inject
 lateinit var appContext: Context
 private val transactionRepository: ITransactionRepository by lazy {
  DependencyManager.provideTransactionRepository(
   appContext
  )
 }

 private val printer: PrinterInterface? by lazy {
  DeviceSDKManager.getPrinterInterface(appContext)
 }

 override fun getViewBinding(
  inflater: LayoutInflater,
  container: ViewGroup?
 ) = FragmentTotalReportByUserIdBinding.inflate(inflater, container, false)

 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)

  viewModel.init()

  binding.back.setOnClickListener {
   finish(this)
  }

  onBackPressed.observe(viewLifecycleOwner, {
   finish(this)
  })

  binding.btnStartDate.setOnClickListener {
   isStartClicked = true
   showDatePicker()
  }

  binding.btnEndDate.setOnClickListener {
   isStartClicked = false
   showDatePicker()
  }

  binding.btnPrintReceipt.setOnClickListener {
   if (startDate != "" && endDate != "") {
    binding.btnPrintReceipt.isEnabled = false
    loading.show(childFragmentManager, "")
    loading.isCancelable = false
    stopTimer()
    val receipt = ReceiptFactory.getReceiptBitmap(requireContext(), initDataForPrinter())
    lifecycleScope.launch(Dispatchers.IO) {
     val result = printer?.printBitmap(receipt) ?: false
     withContext(Dispatchers.Main) {
      startTimer()
      binding.btnPrintReceipt.isEnabled = true
      loading.dismiss()
     }
    }

   } else Utils.makeSnack(
    binding.root,
    "لطفا تاریخ شروع و پایان را انتخاب کنید!",
    Snackbar.LENGTH_SHORT
   ).show()
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
    if (binding.code.text.isNotBlank()) {
     loading.show(childFragmentManager, "")
     loading.isCancelable = false
     getSuccessCount("000000")

    } else {
     Utils.makeSnack(binding.btnStartDate, "لطفا کد صندوق دار وارد نمائید", Snackbar.LENGTH_SHORT)
      .show()
    }
   } else {
    Utils.makeSnack(
     binding.btnStartDate,
     "تاریخ شروع باید قبل تر از تاریخ پایان باشد.",
     Snackbar.LENGTH_SHORT
    ).show()

   }
  }
 }

 override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
  val newMonth = monthOfYear + 1
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
  datePickerDialog.show(childFragmentManager, "")
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

 private fun getSuccessCount(mti: String) {
  lifecycleScope.launch {

   val sDate = startDate.dropLast(4)
   val eDate = endDate.dropLast(4)

   val end = AryanUtils.shamsiToMiladi(
    eDate.split('/')[0].toInt(),
    eDate.split('/')[1].toInt(),
    eDate.split('/')[2].toInt()
   ) + endDate.takeLast(4) + "00"
   val start = AryanUtils.shamsiToMiladi(
    sDate.split('/')[0].toInt(),
    sDate.split('/')[1].toInt(),
    sDate.split('/')[2].toInt()
   ) + startDate.takeLast(4) + "00"
   val userId = binding.code.text.toString().toLong()

   val result = transactionRepository.getSuccessCountForUser(start, end, mti, userId)
   when (mti) {
    "000000" -> {
     binding.successBuy.text = result.toString()
     getSuccessCount("170000")
    }
    "170000" -> {
     binding.successBill.text = result.toString()
     getSuccessCount("220000")
    }
    "220000" -> {
     binding.successTopup.text = result.toString()
     getSuccessCount("180000")
    }
    "180000" -> {
     binding.successCharge.text = result.toString()
     getSum("000000")
    }
   }

  }
 }

 private fun getSum(mti: String) {
  lifecycleScope.launch {

   val sDate = startDate.dropLast(4)
   val eDate = endDate.dropLast(4)

   val end = AryanUtils.shamsiToMiladi(
    eDate.split('/')[0].toInt(),
    eDate.split('/')[1].toInt(),
    eDate.split('/')[2].toInt()
   ) + endDate.takeLast(4) + "00"
   val start = AryanUtils.shamsiToMiladi(
    sDate.split('/')[0].toInt(),
    sDate.split('/')[1].toInt(),
    sDate.split('/')[2].toInt()
   ) + startDate.takeLast(4) + "00"
   val userId = binding.code.text.toString().toLong()

   val result = transactionRepository.getAmountForUser(start, end, mti, userId)
   when (mti) {
    "000000" -> {
     binding.sumBuy.text = RialFormatter.format(
      result.toString()
     )
     getSum("170000")
    }
    "170000" -> {
     binding.sumBill.text = RialFormatter.format(
      result.toString()
     )
     getSum("220000")
    }
    "220000" -> {
     binding.sumTopup.text = RialFormatter.format(
      result.toString()
     )
     getSum("180000")
    }
    "180000" -> {
     binding.sumCharge.text = RialFormatter.format(result.toString())
     binding.lnrData.visibility = View.VISIBLE
     binding.txtEmpty.visibility = View.GONE
     loading.dismiss()
    }
   }
  }
 }

 private fun initDataForPrinter(): HashMap<IsoFields, Any> {

  val data = HashMap<IsoFields, Any>()
  return data.apply {
   put(IsoFields.Type, TransactionType.Total.name)
   put(IsoFields.TypeName, "گزارش سرجمع تراکنش های موفق")
   put(IsoFields.Terminal, viewModel.terminal)
   put(IsoFields.StartDate, binding.txtStartDate.text.toString())
   put(IsoFields.EndDate, binding.txtEndDate.text.toString())

   put(IsoFields.Buffer1, binding.successBuy.text.toString())
   put(IsoFields.Buffer2, binding.sumBuy.text.toString())

   put(IsoFields.Buffer3, binding.successBill.text.toString())
   put(IsoFields.Buffer4, binding.sumBill.text.toString())

   put(IsoFields.Buffer5, binding.successTopup.text.toString())
   put(IsoFields.Buffer6, binding.sumTopup.text.toString())

   put(IsoFields.Buffer7, binding.successCharge.text.toString())
   put(IsoFields.Buffer8, binding.sumCharge.text.toString())
  }
 }

}