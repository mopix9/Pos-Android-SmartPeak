package com.topkishmopix.peak.setting.shop.report.viewmodel

import android.app.Application
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.enum.TransactionReceiptStatus
import com.fanap.corepos.receipt.enum.TransactionType
import com.fanap.corepos.utils.aryan.AryanResponse
import com.fanap.corepos.utils.aryan.AryanUtils
import com.fanap.corepos.utils.sina.SinaUtils
import com.topkishmopix.peak.base.BaseViewModel

class ShowReceiptViewModel(application: Application) : BaseViewModel(application) {

    fun makeReceipt(input: Transaction?): HashMap<IsoFields, Any>? {
        input?.let {
            return when(it.processCode){
                "000000" -> makeBuyReceipt(input)
                "170000" -> makeBillReceipt(input)
                "180000" -> makeChargeReceipt(input,false)
                "220000" -> makeChargeReceipt(input,true)
                else -> null
            }
        }
        return null
    }

    private fun makeBuyReceipt(transaction: Transaction): HashMap<IsoFields, Any> {
        val data = HashMap<IsoFields, Any>()

        when (transaction.response) {
            "00" -> data[IsoFields.Status] =  TransactionReceiptStatus.Success.name
            "-1" -> {
                data[IsoFields.Status] = TransactionReceiptStatus.UnReceivedResponse.name
            }
            else -> {
                data[IsoFields.Status] = TransactionReceiptStatus.Fail.name
            }
        }

        return data.apply {
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
            put(IsoFields.TypeName, "رسید مجدد-خرید")
            put(IsoFields.Merchant, merchant)
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Track2, transaction.card?:"")
            put(IsoFields.Rrn, transaction.rrn ?: "")
            put(IsoFields.Stan, transaction.stan ?: "")
            put(IsoFields.Response, transaction.response ?: "")
        }
    }

    private fun makeBillReceipt(transaction: Transaction): HashMap<IsoFields, Any> {
        val data = HashMap<IsoFields, Any>()

        when (transaction.response) {
            "00" -> data[IsoFields.Status] =  TransactionReceiptStatus.Success.name
            "-1" -> {
                data[IsoFields.Status] = TransactionReceiptStatus.UnReceivedResponse.name
                data[IsoFields.FailMessage] = "خطا در انجام تراکنش"
            }
            else -> {
                data[IsoFields.Status] = TransactionReceiptStatus.Fail.name
                data[IsoFields.FailMessage] = AryanResponse.getResponse(transaction.response ?: "")
            }
        }

        data[IsoFields.Response] = transaction.response ?: ""

        data.apply {
            put(IsoFields.Type, TransactionType.Bill.name)
            put(IsoFields.Amount,transaction.amount?:"")
            put(IsoFields.MerchantName, name)
            put(IsoFields.MerchantPhone, merchantPhone)
            put(IsoFields.TransactionTime, SinaUtils.getTimeForReceipt(transaction.date?.takeLast(6) ?: ""))
            put(IsoFields.TransactionDate, SinaUtils.getShamsiDateFromString(transaction.date?.take(6)?: ""))
            put(IsoFields.TypeName, transaction.description ?: "")
            put(IsoFields.Track2,transaction.card ?: "")
            put(IsoFields.BillId, transaction.description2 ?: "")
            put(IsoFields.PayId, transaction.description3 ?: "")
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Rrn, transaction.rrn ?: "")
            put(IsoFields.Stan, transaction.stan ?: "")
            put(IsoFields.IsAgainReceipt, true.toString())
        }
        return data
    }

    private fun makeChargeReceipt(transaction: Transaction, isTopUp: Boolean): HashMap<IsoFields, Any> {
        val data = HashMap<IsoFields, Any>()

        when (transaction.response) {
            "00" -> data[IsoFields.Status] =  TransactionReceiptStatus.Success.name
            "-1" -> {
                data[IsoFields.Status] = TransactionReceiptStatus.UnReceivedResponse.name
                data[IsoFields.FailMessage] = "خطا در انجام تراکنش"
            }
            else -> {
                data[IsoFields.Status] = TransactionReceiptStatus.Fail.name
                data[IsoFields.FailMessage] = AryanResponse.getResponse(transaction.response ?: "")
            }
        }

        if(isTopUp)
            data[IsoFields.Type] =  TransactionType.Topup.name
        else
            data[IsoFields.Type] =  TransactionType.Voucher.name

        data.apply {
            put(IsoFields.Amount,transaction.amount ?: "")
            put(IsoFields.MerchantName, name)
            put(IsoFields.MerchantPhone, merchantPhone)
            put(IsoFields.TransactionTime, SinaUtils.getTimeForReceipt(transaction.date?.takeLast(6) ?: ""))
            put(IsoFields.TransactionDate, SinaUtils.getShamsiDateFromString(transaction.date?.take(6)?: ""))
            put(IsoFields.TypeName, "خرید شارژ")
            put(IsoFields.Track2,transaction.card ?: "")
            put(IsoFields.ChargePin,"468464535448548")
            put(IsoFields.ChargeOrganization, transaction.description ?: "")
            put(IsoFields.PhoneNumber, transaction.description2 ?: "")
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Rrn, transaction.rrn ?: "")
            put(IsoFields.Stan, transaction.stan ?: "")
            put(IsoFields.IsAgainReceipt, true.toString())
        }
        return data
    }
}