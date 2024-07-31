package com.topkishmopix.peak.base

import android.annotation.SuppressLint
import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fanap.corepos.database.base.ISettingsRepository
import com.fanap.corepos.database.base.ITransactionRepository
import com.fanap.corepos.database.base.IUserRepository
import com.fanap.corepos.database.service.model.SettingsNames
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.utils.IsoFields
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@SuppressLint("SuspiciousIndentation")
open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionRepository: ITransactionRepository by lazy { DependencyManager.provideTransactionRepository(getApplication()) }
    val settingsRepository: ISettingsRepository by lazy { DependencyManager.provideSettingsRepository(getApplication()) }
    val userRepository: IUserRepository by lazy { DependencyManager.provideUserRepository(getApplication()) }





    lateinit var serial : String
    lateinit var terminal : String
    lateinit var merchant : String
    lateinit var ip : String
    lateinit var port : String
    lateinit var nii : String
    lateinit var name : String
    lateinit var merchantPhone : String
    lateinit var stanList : List<Int>

    /***********************************************/


    var observableTerminal = ObservableField("")
    var observableMerchant = ObservableField("")
    var observableSerial = ObservableField("")
    var observableName = ObservableField("")
    var observablePhone = ObservableField("")
    var observableIp = ObservableField("")
    var observablePort = ObservableField("")
    var observableNii = ObservableField("")



    init {




        viewModelScope.launch {
            stanList = transactionRepository.getStanSet()


//         serial =  "00001504P6000003690"
         serial =  settingsRepository.getValue(SettingsNames.TerminalSerial.name)?.value ?: ""
            observableSerial.set(serial)

            terminal = settingsRepository.getValue(SettingsNames.TerminalNo.name)?.value ?: ""
            observableTerminal.set(terminal)

            name = settingsRepository.getValue(SettingsNames.MerchantName.name)?.value ?: ""
            observableName.set(name)

            merchant = settingsRepository.getValue(SettingsNames.MerchantNo.name)?.value ?: ""
            observableMerchant.set(merchant)

            merchantPhone = settingsRepository.getValue(SettingsNames.Phone.name)?.value ?: ""
            observablePhone.set(merchantPhone)

            ip = settingsRepository.getValue(SettingsNames.Ip.name)?.value ?: ""
            observableIp.set(ip)

            port = settingsRepository.getValue(SettingsNames.Port.name)?.value ?: ""
            observablePort.set(port)

            nii = settingsRepository.getValue(SettingsNames.Nii.name)?.value ?: ""
            observableNii.set(nii)
        }
    }

    suspend fun insertTransaction(map : HashMap<IsoFields, String>) = viewModelScope.async(
        Dispatchers.IO) {
        return@async transactionRepository.insert(map)
    }.await()

    suspend fun updateTransaction(transaction: Transaction) = viewModelScope.async(Dispatchers.IO) {
        return@async transactionRepository.updateTransaction(transaction)
    }.await()

    open fun makeAdvice(transaction: Transaction, track2: String) = HashMap<IsoFields, String>().apply {
        put(IsoFields.Mti,"0220")
        put(IsoFields.ProcessCode,"000000")
        put(IsoFields.Amount, transaction.amount ?: "")
        put(IsoFields.Stan, transaction.stan?:"0")
        put(IsoFields.TransactionTime, transaction.date?.takeLast(6) ?: "")
        put(IsoFields.TransactionDate, transaction.date?.take(4) ?: "")
        put(IsoFields.NiiCode, "09")
        put(IsoFields.ConditionCode, "00")
        put(IsoFields.Terminal, terminal)
        put(IsoFields.Merchant, merchant)
        put(IsoFields.ConnectionType, "4")
        put(IsoFields.CurrencyCode, "364")
    }

    open fun makeReverse(transaction: Transaction) = HashMap<IsoFields, String>().apply {
        put(IsoFields.Mti,"0420")
        put(IsoFields.ProcessCode,"000000")
        put(IsoFields.Amount, transaction.amount ?: "")
        put(IsoFields.Stan, transaction.stan?:"0")
        put(IsoFields.TransactionTime, transaction.date?.takeLast(6) ?: "")
        put(IsoFields.TransactionDate, transaction.date?.take(4) ?: "")
        put(IsoFields.NiiCode, "09")
        put(IsoFields.ConditionCode, "00")
        put(IsoFields.Terminal, terminal)
        put(IsoFields.Merchant, merchant)
        put(IsoFields.ConnectionType, "4")
        put(IsoFields.CurrencyCode, "364")
    }


    fun init(){}


    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}