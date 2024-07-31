package com.topkishmopix.peak.bill.viewmodel

import android.app.Application
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.database.service.model.TransactionStatus
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.enum.TransactionReceiptStatus
import com.fanap.corepos.receipt.enum.TransactionType
import com.fanap.corepos.utils.aryan.AryanUtils
import com.fanap.corepos.utils.sina.SinaUtils
import com.topkishmopix.peak.BuildConfig
import com.topkishmopix.peak.base.BaseViewModel

class BillDetailViewModel(application: Application) : BaseViewModel(application) {

    fun makeTransaction(track2: String, pinBlock: String, amount : String ,billId:String, payId : String) =
        HashMap<IsoFields, String>().apply {
            put(IsoFields.Mti,"0200")
            put(IsoFields.ProcessCode, "170000")
            put(IsoFields.Amount,amount)
            put(IsoFields.Stan, stanList[1].toString())
            put(IsoFields.TransactionTime, AryanUtils.getTime())
            put(IsoFields.TransactionDate, AryanUtils.getDate())
            put(IsoFields.InformationEntryMode, "21")
            put(IsoFields.NiiCode, DependencyManager.nii)
            put(IsoFields.ConditionCode, "00")
            put(IsoFields.Track2,track2.replace('=', 'D'))
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Merchant, merchant)
            put(IsoFields.Serial, serial)
            put(IsoFields.SoftwareVersion, BuildConfig.VERSION_CODE.toString())
            put(IsoFields.TerminalLanguageCode, "0")
            put(IsoFields.BillId,billId)
            put(IsoFields.PayId,payId)
            put(IsoFields.ConnectionType, "4")
            put(IsoFields.CurrencyCode, "364")
            put(IsoFields.PinBlock,pinBlock)
            put(IsoFields.Status, TransactionStatus.TransactionSent.name)
        }



    fun makeReceipt(track2: String, transaction: Transaction): HashMap<IsoFields, String> {

        val data = HashMap<IsoFields, String>()
        data.apply {
            put(IsoFields.Type, TransactionType.Bill.name)
            put(IsoFields.Amount,transaction.amount?:"")
            put(IsoFields.MerchantName, name)
            put(IsoFields.MerchantPhone, merchantPhone)
            put(IsoFields.Merchant, merchant)
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Rrn,transaction.rrn ?: "")
            put(IsoFields.Response,transaction.response ?: "")
            put(IsoFields.Stan,transaction.stan ?: "")
            put(
                IsoFields.TransactionTime,
                AryanUtils.getTimeForReceipt(transaction.date ?: "000000")
            )
            put(
                IsoFields.TransactionDate,
                AryanUtils.getShamsiDateFromString(transaction.date ?: "0000")
            )
            put(IsoFields.TransactionTime, StringBuilder(SinaUtils.getTime().take(4)).insert(2, ":").toString())
            put(IsoFields.TransactionDate, SinaUtils.getPersianDate())
            put(IsoFields.TypeName, "رسید مشتری-قبض")
            put(IsoFields.Buffer1, transaction.description?:"")
            put(IsoFields.Status, TransactionReceiptStatus.Success.name)
            put(IsoFields.Track2,AryanUtils.maskCard(track2) ?: "")
            put(IsoFields.BillId,transaction.description2?:"")
            put(IsoFields.PayId,transaction.description3?:"")

        }
        return data
    }
}