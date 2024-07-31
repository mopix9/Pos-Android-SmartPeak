package com.topkishmopix.peak.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.fanap.corepos.database.base.ISettingsRepository
import com.fanap.corepos.database.base.ITransactionRepository
import com.fanap.corepos.database.service.model.SettingsNames
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.database.service.model.TransactionStatus
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.IIso
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.utils.sina.SinaUtils
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

object TransactionChecker : CoroutineScope {

    private val TAG = this::class.java.simpleName

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    var callBack = MutableLiveData<TransactionCheckerStatus>()

    private lateinit var settingsRepository: ISettingsRepository
    private lateinit var transactionRepository: ITransactionRepository
    private val transactionManager: IIso by lazy { DependencyManager.provideIsoTransaction() }
    private lateinit var terminal: String
    private lateinit var merchant: String
    private var transaction: Transaction? = null

    fun check(context: Context) {
        settingsRepository = DependencyManager.provideSettingsRepository(context)
        transactionRepository = DependencyManager.provideTransactionRepository(context)

        checkLastTransaction()

        Log.d("lastTransaction", checkLastTransaction().toString())


    }

    private fun checkLastTransaction() {
        cancel()
        launch {
            terminal = settingsRepository.getValue(SettingsNames.TerminalNo.name)?.value ?: ""
            merchant = settingsRepository.getValue(SettingsNames.MerchantNo.name)?.value ?: ""

            transaction = transactionRepository.getLastBuyTransaction()
            transaction?.let {
                if (transaction?.response == "00") {
                    if (transaction?.status != TransactionStatus.AdviceResUnpacked.name)
                        startAdvice(transaction!!)
                    else callBack.postValue(TransactionCheckerStatus.HideIcon)
                } else if (transaction?.response == "-1") {
                    if (transaction?.status != TransactionStatus.ReverseResUnpacked.name)
                        startReverse(transaction!!)
                    else callBack.postValue(TransactionCheckerStatus.HideIcon)
                } else callBack.postValue(TransactionCheckerStatus.HideIcon)
            }
        }
    }

    private fun startReverse(transaction: Transaction) {
        callBack.postValue(TransactionCheckerStatus.ShowReverseIcon)
        val reverseMap = makeReverse(transaction)
        doTransaction(reverseMap)
    }

    private fun startAdvice(transaction: Transaction) {
        callBack.postValue(TransactionCheckerStatus.ShowAdviceIcon)
        val adviceMap = makeAdvice(transaction)
        doTransaction(adviceMap)
    }

    private fun doTransaction(adviceMap: HashMap<IsoFields, String>) {
        launch(Dispatchers.IO) {
            val result = transactionManager.doTransaction(adviceMap)
            withContext(Dispatchers.Main) {
                if (result != null) {
                    val responseCode = result[IsoFields.Response] ?: ""
                    if (SinaUtils.isSuccessfulResponseForConfirmAndReverse(responseCode)) {
                        if (transaction?.response == "00")
                            transaction?.status = TransactionStatus.AdviceResUnpacked.name
                        else
                            transaction?.status = TransactionStatus.ReverseResUnpacked.name
                        transactionRepository.updateTransaction(transaction!!)
                        callBack.postValue(TransactionCheckerStatus.Finished)
                        cancel()
                    } else
                        checkLastTransaction()
                } else
                    checkLastTransaction()
            }
        }
    }

    private fun makeAdvice(transaction: Transaction) = HashMap<IsoFields, String>().apply {
        put(IsoFields.Mti, "0220")
        put(IsoFields.ProcessCode, "000000")
        put(IsoFields.Amount, transaction.amount ?: "")
        put(IsoFields.Stan, transaction.stan ?: "0")
        put(IsoFields.TransactionTime, transaction.date?.takeLast(6) ?: "")
        put(IsoFields.TransactionDate, transaction.date?.take(4) ?: "")
        put(IsoFields.NiiCode, "09")
        put(IsoFields.ConditionCode, "00")
        put(IsoFields.Terminal, terminal)
        put(IsoFields.Merchant, merchant)
        put(IsoFields.ConnectionType, "4")
        put(IsoFields.CurrencyCode, "364")
    }

    private fun makeReverse(transaction: Transaction) = HashMap<IsoFields, String>().apply {
        put(IsoFields.Mti, "0420")
        put(IsoFields.ProcessCode, "000000")
        put(IsoFields.Amount, transaction.amount ?: "")
        put(IsoFields.Stan, transaction.stan ?: "0")
        put(IsoFields.TransactionTime, transaction.date?.takeLast(6) ?: "")
        put(IsoFields.TransactionDate, transaction.date?.take(4) ?: "")
        put(IsoFields.NiiCode, "09")
        put(IsoFields.ConditionCode, "00")
        put(IsoFields.Terminal, terminal)
        put(IsoFields.Merchant, merchant)
        put(IsoFields.ConnectionType, "4")
        put(IsoFields.CurrencyCode, "364")
    }

    sealed class
    TransactionCheckerStatus {
        object ShowReverseIcon : TransactionCheckerStatus()
        object ShowAdviceIcon : TransactionCheckerStatus()
        object HideIcon : TransactionCheckerStatus()
        object Finished : TransactionCheckerStatus()
    }
}