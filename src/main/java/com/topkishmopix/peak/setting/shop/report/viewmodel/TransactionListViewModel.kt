package com.topkishmopix.peak.setting.shop.report.viewmodel

import android.app.Application
import com.fanap.corepos.database.base.ITransactionRepository
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.enum.PrintPart
import com.fanap.corepos.receipt.enum.TransactionType
import com.topkishmopix.peak.base.BaseViewModel

class TransactionListViewModel(application: Application) : BaseViewModel(application) {

    lateinit var type : String

    private val transactionRepository: ITransactionRepository by lazy { DependencyManager.provideTransactionRepository(application) }


    fun makeAllReceipt(transactions: List<Transaction>,startDate : String, endDate: String): HashMap<IsoFields, Any> {
        val data = HashMap<IsoFields, Any>()

        return data.apply {
            put(IsoFields.Type, TransactionType.DetailList.name)
            put(IsoFields.TypeName, getTitle())
            put(IsoFields.Terminal, terminal)
            put(IsoFields.StartDate, startDate)
            put(IsoFields.EndDate, endDate)
            put(IsoFields.Buffer1, PrintPart.All.name)

            put(IsoFields.Buffer2, transactions)

            put(IsoFields.Buffer3, transactions.size.toString())
            put(IsoFields.Amount, transactions.sumOf { it.amount?.toLong()?:0 }.toString())
        }
    }

    fun makeHeaderReceipt(transactions: List<Transaction>,startDate : String, endDate: String): HashMap<IsoFields, Any> {
        val data = HashMap<IsoFields, Any>()

        return data.apply {
            put(IsoFields.Type, TransactionType.DetailList.name)
            put(IsoFields.TypeName, getTitle())
            put(IsoFields.Terminal, terminal)
            put(IsoFields.StartDate, startDate)
            put(IsoFields.EndDate, endDate)
            put(IsoFields.Buffer1, PrintPart.Header.name)

            put(IsoFields.Buffer2, transactions)
        }
    }

    fun makeBodyReceipt(transactions: List<Transaction>): HashMap<IsoFields, Any> {
        val data = HashMap<IsoFields, Any>()
        return data.apply {
            put(IsoFields.Type, TransactionType.DetailList.name)
            put(IsoFields.Buffer1, PrintPart.Body.name)
            put(IsoFields.Buffer2, transactions)
        }
    }

    fun makeFooterReceipt(transactions: List<Transaction>,amount : String, count: String): HashMap<IsoFields, Any> {
        val data = HashMap<IsoFields, Any>()

        return data.apply {
            put(IsoFields.Type, TransactionType.DetailList.name)
            put(IsoFields.Buffer1, PrintPart.Footer.name)
            put(IsoFields.Buffer2, transactions)
            put(IsoFields.Buffer3, count)
            put(IsoFields.Amount, amount)

        }
    }

    suspend fun getSuccessTransactionsLazy(start: String, end: String, l: Long) : List<Transaction>? {
        return if (type == ""){
            transactionRepository.getSuccessTransactionsLazyAll(start,end,l)
        }else{
            transactionRepository.getSuccessTransactionsLazy(start,end,type,l)
        }
    }

    suspend fun getSuccessTransactions(start: String, end: String)  : List<Transaction>? {
        return if (type == ""){
            transactionRepository.getSuccessTransactionsAll(start,end)
        }else{
            transactionRepository.getSuccessTransactions(start,end,type)
        }
    }


    fun getTitle() = when(type){
        "000000" -> "خرید"
        "170000" -> "پرداخت قبض"
        "180000" -> "شارژ وچر"
        "220000" -> "شارژ مستقیم"
        else -> " همه"
    }
}