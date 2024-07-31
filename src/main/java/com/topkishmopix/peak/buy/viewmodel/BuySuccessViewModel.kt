package com.topkishmopix.peak.buy.viewmodel

import android.app.Application
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.enum.TransactionReceiptStatus
import com.fanap.corepos.receipt.enum.TransactionType
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.aryan.AryanUtils
import com.topkishmopix.peak.base.BaseViewModel

class BuySuccessViewModel(application: Application) : BaseViewModel(application) {





    fun makeReceipt(track2: String, transaction : HashMap<IsoFields, String>, amount : String): HashMap<IsoFields, Any> =

        HashMap<IsoFields, Any>().apply {
            put(IsoFields.Type, TransactionType.Buy.name)
            put(IsoFields.Amount, amount)
            put(IsoFields.MerchantName, name)
            put(IsoFields.MerchantPhone, merchantPhone)
            put(
                IsoFields.TransactionTime,
                AryanUtils.getTimeForReceipt(transaction[IsoFields.TransactionTime] ?: "000000")
            )
            put(
                IsoFields.TransactionDate, AryanUtils.getShamsiDateFromString(transaction[IsoFields.TransactionDate] ?: "0000"))
            put(IsoFields.TypeName, "رسید پذیرنده-خرید")
            put(IsoFields.Merchant, merchant)
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Track2, AryanUtils.maskCard(track2) ?: "")
            put(IsoFields.Rrn, transaction[IsoFields.Rrn] ?:"")
            put(IsoFields.Stan, Utils.removeZeros(transaction[IsoFields.Stan] ?: "") )

            put(IsoFields.Status, TransactionReceiptStatus.Merchant.name)

        }
}