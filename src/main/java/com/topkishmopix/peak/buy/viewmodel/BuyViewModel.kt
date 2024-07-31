package com.topkishmopix.peak.buy.viewmodel

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.database.service.model.TransactionStatus
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.enum.TransactionReceiptStatus
import com.fanap.corepos.receipt.enum.TransactionType
import com.fanap.corepos.utils.aryan.AryanUtils
import com.topkishmopix.peak.BuildConfig
import com.topkishmopix.peak.base.BaseViewModel

class BuyViewModel(application: Application) : BaseViewModel(application) {

    var amount = ObservableField("")
    var onConfirmClicked: MutableLiveData<Boolean> = MutableLiveData()
    var onError: MutableLiveData<String> = MutableLiveData()


    init {
//        Utils.openKeyboard(getApplication())
    }

    fun setOnConfirmClicked() {

//        Utils.hideKeyboard(getApplication())
        val temp = amount.get()?.replace(",", "") ?: ""
        if (temp.isNotBlank()) {
            if (temp.toLong() <= 500_000_000) {
                onConfirmClicked.postValue(true)
            } else onError.postValue("لطفا مبلغ معتبر وارد کنید.")
        } else onError.postValue("لطفا مبلغ معتبر وارد کنید.")
    }

    fun makeTransaction(track2: String, pinBlock: String) =
        HashMap<IsoFields, String>().apply {
            put(IsoFields.Mti, "0200")
            put(IsoFields.ProcessCode, "000000")
            put(IsoFields.Stan, stanList[1].toString())
            put(IsoFields.TransactionTime, AryanUtils.getTime())
            put(IsoFields.TransactionDate, AryanUtils.getDate())
            put(IsoFields.Track2, track2.replace('=', 'D'))
            put(IsoFields.Serial, serial)
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Merchant, merchant)
            put(IsoFields.PinBlock, pinBlock)
            put(IsoFields.Amount, amount.get()?.replace(",", "") ?: "0")
            put(IsoFields.InformationEntryMode, "21")
            put(IsoFields.ConditionCode, "00")
            put(IsoFields.NiiCode, DependencyManager.nii)
            put(IsoFields.SoftwareVersion, BuildConfig.VERSION_CODE.toString())
            put(IsoFields.TerminalLanguageCode, "0")
            put(IsoFields.ConnectionType, "4")
            put(IsoFields.CurrencyCode, "364")
            put(IsoFields.Status, TransactionStatus.TransactionSent.name)
        }

    fun makeReceipt(track2: String, transaction: Transaction): HashMap<IsoFields, Any> =
        HashMap<IsoFields, Any>().apply {
            put(IsoFields.Type, TransactionType.Buy.name)
            put(IsoFields.Amount, transaction.amount ?: "")
            put(IsoFields.MerchantName, name)
            put(IsoFields.MerchantPhone, merchantPhone)
            put(
                IsoFields.TransactionTime,
                AryanUtils.getTimeForReceipt(transaction.date?.takeLast(6) ?: "000000")
            )
            put(
                IsoFields.TransactionDate,
                AryanUtils.getShamsiDateFromString(transaction.date?.take(4) ?: "0000")
            )
            put(IsoFields.TypeName, "رسید مشتری-خرید")
            put(IsoFields.Merchant, merchant)
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Track2, AryanUtils.maskCard(track2) ?: "")
            put(IsoFields.Rrn, transaction.rrn ?: "")
            put(IsoFields.Stan, transaction.stan ?: "")

            put(IsoFields.Status, TransactionReceiptStatus.Success.name)
        }

    fun makeFailReceipt(track2: String, transaction: Transaction): HashMap<IsoFields, Any> =
        HashMap<IsoFields, Any>().apply {
            put(IsoFields.Type, TransactionType.Buy.name)
            put(IsoFields.Amount, transaction.amount ?: "")
            put(IsoFields.MerchantName, name)
            put(IsoFields.MerchantPhone, merchantPhone)
            put(
                IsoFields.TransactionTime,
                AryanUtils.getTimeForReceipt(transaction.date?.takeLast(6) ?: "000000")
            )
            put(
                IsoFields.TransactionDate,
                AryanUtils.getShamsiDateFromString(transaction.date?.take(4) ?: "0000")
            )
            put(IsoFields.TypeName, "رسید مشتری-خرید")
            put(IsoFields.Merchant, merchant)
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Track2, AryanUtils.maskCard(track2)?:"")
            put(IsoFields.Rrn, transaction.rrn ?: "")
            put(IsoFields.Stan, transaction.stan ?: "")
            put(IsoFields.Response, transaction.response ?: "")

            put(IsoFields.Status, TransactionReceiptStatus.Fail.name)
        }

    fun makeNullResponseReceipt(track2: String, transaction: Transaction): HashMap<IsoFields, Any> =
        HashMap<IsoFields, Any>().apply {
            put(IsoFields.Type, TransactionType.Buy.name)
            put(IsoFields.Amount, transaction.amount ?: "")
            put(IsoFields.MerchantName, name)
            put(IsoFields.MerchantPhone, merchantPhone)
            put(
                IsoFields.TransactionTime,
                AryanUtils.getTimeForReceipt(transaction.date?.takeLast(6) ?: "000000")
            )
            put(
                IsoFields.TransactionDate,
                AryanUtils.getShamsiDateFromString(transaction.date?.take(4) ?: "0000")
            )
            put(IsoFields.TypeName, "رسید مشتری-خرید")
            put(IsoFields.Merchant, merchant)
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Track2, AryanUtils.maskCard(track2)?:"")
            put(IsoFields.Rrn, transaction.rrn ?: "")
            put(IsoFields.Stan, transaction.stan ?: "")
            put(IsoFields.Response, transaction.response ?: "")

            put(IsoFields.Status, TransactionReceiptStatus.UnReceivedResponse.name)
        }
}