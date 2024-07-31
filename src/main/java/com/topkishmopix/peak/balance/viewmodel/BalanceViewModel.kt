package com.topkishmopix.peak.balance.viewmodel

import android.app.Application
import com.fanap.corepos.database.service.model.TransactionStatus
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.enum.TransactionReceiptStatus
import com.fanap.corepos.receipt.enum.TransactionType
import com.fanap.corepos.utils.aryan.AryanUtils
import com.topkishmopix.peak.BuildConfig
import com.topkishmopix.peak.base.BaseViewModel


class BalanceViewModel(application: Application) : BaseViewModel(application) {

    fun makeTransaction(track2: String, pinBlock: String) =
        HashMap<IsoFields, String>().apply {
            put(IsoFields.Mti,"0100")
            put(IsoFields.Pan,track2.split("=")[0])
            put(IsoFields.ProcessCode, "310000")
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
//            put(IsoFields.Serial, "00001504P6000003690")//p600
            put(IsoFields.SoftwareVersion, BuildConfig.VERSION_CODE.toString())
            put(IsoFields.TerminalLanguageCode, "0")
            put(IsoFields.ConnectionType, "7")
            put(IsoFields.PinBlock,pinBlock)
            put(IsoFields.Status, TransactionStatus.TransactionSent.name)

        }

    fun makeReceipt(track2: String, transaction: HashMap<IsoFields, String>): HashMap<IsoFields, String> {
        val data = HashMap<IsoFields, String>()
        data.apply {
            put(IsoFields.Type,TransactionType.Balance.name)
            put(IsoFields.Balance,transaction[IsoFields.Balance] ?: "")
            put(IsoFields.MerchantName, name)
            put(IsoFields.Merchant, merchant)
            put(IsoFields.MerchantPhone, merchantPhone)
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Rrn,transaction[IsoFields.Rrn] ?: "")
            put(IsoFields.Stan,transaction[IsoFields.Stan] ?: "")
            put(
                IsoFields.TransactionTime,
                AryanUtils.getTimeForReceipt(transaction[IsoFields.TransactionTime] ?: "000000")
            )
            put(
                IsoFields.TransactionDate,
                AryanUtils.getShamsiDateFromString(transaction[IsoFields.TransactionDate] ?: "0000")
            )
            put(IsoFields.TypeName, "رسید مشتری-موجودی")
            put(IsoFields.Status, TransactionReceiptStatus.Success.name)
            put(IsoFields.Track2,AryanUtils.maskCard(track2) ?: "")
        }
        return data
    }

}