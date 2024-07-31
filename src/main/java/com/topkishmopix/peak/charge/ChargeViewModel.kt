package com.topkishmopix.peak.charge

import android.app.Application
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.fanap.corepos.database.service.model.TransactionStatus
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.receipt.enum.ChargeOrganization
import com.fanap.corepos.receipt.enum.TransactionReceiptStatus
import com.fanap.corepos.receipt.enum.TransactionType
import com.fanap.corepos.utils.Operator
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.aryan.AryanUtils
import com.fanap.corepos.utils.sayan.SayanUtils
import com.topkishmopix.peak.BuildConfig
import com.topkishmopix.peak.base.BaseViewModel
import kotlin.math.roundToInt

class ChargeViewModel(application: Application) : BaseViewModel(application) {
    ////////////General////////////////
    var isTopUp: ObservableBoolean
    var codeTabClicked: MutableLiveData<Boolean>

    /////////////Top up/////////////////////////
    var amount: ObservableInt
    var isMci: ObservableBoolean
    var isMtn: ObservableBoolean
    var isRightel: ObservableBoolean
    var isTalia: ObservableBoolean
    var phone: ObservableField<String>

    /////////////Charge code////////////////////
    var isMciChargeCode: ObservableBoolean
    var isMtnChargeCode: ObservableBoolean
    var isRightelChargeCode: ObservableBoolean
    var isTaliaChargeCode: ObservableBoolean

    ////////////General////////////////
    fun onTabClicked(isTopUpClicked: Boolean) {
        isTopUp.set(isTopUpClicked)
        if (!isTopUpClicked) codeTabClicked.postValue(true)
    }

    ////////////////////////Charge Code//////////////////////////////////////
    fun toggleChargeCode(type: Int) {
        if (type == 1) {
            isMciChargeCode.set(!isMciChargeCode.get())
            isMtnChargeCode.set(false)
            isRightelChargeCode.set(false)
            isTaliaChargeCode.set(false)
        } else if (type == 2) {
            isMciChargeCode.set(false)
            isMtnChargeCode.set(!isMtnChargeCode.get())
            isRightelChargeCode.set(false)
            isTaliaChargeCode.set(false)
        } else if (type == 3) {
            isMciChargeCode.set(false)
            isMtnChargeCode.set(false)
            isTaliaChargeCode.set(false)
            isRightelChargeCode.set(!isRightelChargeCode.get())
        } else{
            isMciChargeCode.set(false)
            isMtnChargeCode.set(false)
            isTaliaChargeCode.set(!isTaliaChargeCode.get())
            isRightelChargeCode.set(false)
        }
    }

    /////////////Top up/////////////////////////
    fun onEdittextChanged(phoneNumber: CharSequence) {
        try {
            if (phoneNumber.length >= 4) {
                val prefix = phoneNumber.toString().trim { it <= ' ' }
                    .substring(0, 4)
                val mciList = Utils.MCI_PREFIXES
                val mtnList = Utils.MTN_PREFIXES
                val rightelList = Utils.RIGHTEL_PREFIXES
                if (mciList.contains(prefix)) {
                    isMci.set(true)
                    isMtn.set(false)
                    isRightel.set(false)
                } else if (mtnList.contains(prefix)) {
                    isMci.set(false)
                    isMtn.set(true)
                    isRightel.set(false)
                } else if (rightelList.contains(prefix)) {
                    isMci.set(false)
                    isMtn.set(false)
                    isRightel.set(true)
                } else {
                    isMci.set(false)
                    isMtn.set(false)
                    isRightel.set(false)
                }
            } else {
                isMci.set(false)
                isMtn.set(false)
                isRightel.set(false)
            }

//            if (phoneNumber.length() >= 11){
//                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val isMciTopUp: Unit
        get() {
            isMci.set(!isMci.get())
            isMtn.set(false)
            isRightel.set(false)
            isTalia.set(false)
        }
    val isMtnTopUp: Unit
        get() {
            isMtn.set(!isMtn.get())
            isMci.set(false)
            isRightel.set(false)
            isTalia.set(false)
        }
    val isRightelTopUp: Unit
        get() {
            isRightel.set(!isRightel.get())
            isMci.set(false)
            isMtn.set(false)
            isTalia.set(false)
        }

    val isTaliaTopUp: Unit
        get() {
            isTalia.set(!isTalia.get())
            isMci.set(false)
            isMtn.set(false)
            isRightel.set(false)
        }

    val topupOperator: Operator?
        get() = if (isMci.get()) Operator.MCI else if (isMtn.get()) Operator.MTN else if (isRightel.get()) Operator.RIGHTEL else if (isTalia.get()) Operator.TALIA else null
    val chargeCodeOperator: Operator?
        get() = if (isMciChargeCode.get()) Operator.MCI else if (isMtnChargeCode.get()) Operator.MTN else if (isRightelChargeCode.get()) Operator.RIGHTEL else if (isTaliaChargeCode.get()) Operator.TALIA else null

    init {

        ////////////General////////////////
        isTopUp = ObservableBoolean(true)
        codeTabClicked = MutableLiveData()
        /////////////Top up/////////////////////////
        amount = ObservableInt(0)
        isMci = ObservableBoolean(false)
        isMtn = ObservableBoolean(false)
        isRightel = ObservableBoolean(false)
        isTalia = ObservableBoolean(false)
        phone = ObservableField("")

        ////////////////////////Charge Code////////////////////
        isMciChargeCode = ObservableBoolean(true)
        isMtnChargeCode = ObservableBoolean(false)
        isRightelChargeCode = ObservableBoolean(false)
        isTaliaChargeCode = ObservableBoolean(false)
    }

    fun makeTransaction(track2: String, pinBlock: String,amount : String) =
        HashMap<IsoFields, String>().apply {
            put(IsoFields.Mti,"0200")
            put(IsoFields.Stan, stanList[1].toString())
            put(IsoFields.TransactionTime, SayanUtils.getTime())
            put(IsoFields.TransactionDate, SayanUtils.getDate())
            put(IsoFields.InformationEntryMode, "21")
            put(IsoFields.NiiCode, DependencyManager.nii)
            put(IsoFields.ConditionCode, "00")
            put(IsoFields.Track2,track2.replace('=', 'D'))
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Merchant, merchant)
            put(IsoFields.Serial, serial)
            put(IsoFields.SoftwareVersion, BuildConfig.VERSION_CODE.toString())
            put(IsoFields.TerminalLanguageCode, "0")
            put(IsoFields.ConnectionType, "4")
            put(IsoFields.CurrencyCode, "364")
            put(IsoFields.PinBlock,pinBlock)
            put(IsoFields.Status, TransactionStatus.TransactionSent.name)

            if (isTopUp.get()){
                put(IsoFields.Amount, amount)
                put(IsoFields.ProcessCode, "220000")
                put(IsoFields.PhoneNumber, phone.get()?: "")
                if (isMci.get())
                    put(IsoFields.ChargeOrganization,"120")
                else if(isMtn.get())
                    put(IsoFields.ChargeOrganization,"110")
                else if (isRightel.get())
                    put(IsoFields.ChargeOrganization,"170")
            }else{
                put(IsoFields.ProcessCode, "180000")

                if (isMciChargeCode.get()) {
                    put(IsoFields.Amount, amount)
                    put(IsoFields.ChargeOrganization, "12${calcChargeAmountCode(amount)}")
                }
                else if(isMtnChargeCode.get()) {
                    val amountForIrancel = amount.toInt() + (amount.toInt()*0.09).roundToInt()
                    put(IsoFields.Amount, amountForIrancel.toString())
                    put(IsoFields.ChargeOrganization, "11${calcChargeAmountCode(amount)}")
                }
                else if (isRightelChargeCode.get()) {
                    put(IsoFields.Amount, amount)
                    put(IsoFields.ChargeOrganization, "17${calcChargeAmountCode(amount)}")
                }
            }

        }
    fun makeReceipt(track2: String, data: HashMap<IsoFields, String>, amount: String): HashMap<IsoFields, String> {
        if(isTopUp.get()) {
            data[IsoFields.Type] = TransactionType.Topup.name
            data[IsoFields.TypeName] =  "شارژ مستقیم"
        }
        else {
            data[IsoFields.Type] = TransactionType.Voucher.name
            data[IsoFields.TypeName] =  "شارژ وچر-${getChargeOrganization()}"
        }

        data.apply {
            put(IsoFields.Amount,amount)
            put(IsoFields.MerchantName, name)
            put(IsoFields.MerchantPhone, merchantPhone)
            put(IsoFields.Status, TransactionReceiptStatus.Success.name)
            put(IsoFields.Track2,track2)
            put(IsoFields.ChargeOrganization,getChargeOrganization())
            put(IsoFields.PhoneNumber,phone.get()?:"")

            put(IsoFields.Merchant, merchant)
            put(IsoFields.Terminal, terminal)
            put(IsoFields.Rrn,data[IsoFields.Rrn] ?: "")
            put(IsoFields.Stan,data[IsoFields.Stan] ?: "")
            put(
                IsoFields.TransactionTime,
                AryanUtils.getTimeForReceipt(data[IsoFields.TransactionTime] ?: "000000")
            )
            put(
                IsoFields.TransactionDate,
                AryanUtils.getShamsiDateFromString(data[IsoFields.TransactionDate] ?: "0000")
            )
            put(IsoFields.Status, TransactionReceiptStatus.Success.name)
            put(IsoFields.Track2, AryanUtils.maskCard(track2) ?: "")

        }
        return data
    }

    private fun getChargeOrganization(): String{
        if (isTopUp.get()){
            if (isMci.get())
                return ChargeOrganization.HAMRAH_AVAL.chargeName
            if (isMtn.get())
                return ChargeOrganization.IRANCELL.chargeName
            if (isRightel.get())
                return ChargeOrganization.RIGHTEL.chargeName
            if (isTalia.get())
                return ChargeOrganization.TALIA.chargeName
        }else{
            if (isMciChargeCode.get())
                return ChargeOrganization.HAMRAH_AVAL.chargeName
            if (isMtnChargeCode.get())
                return ChargeOrganization.IRANCELL.chargeName
            if (isRightelChargeCode.get())
                return ChargeOrganization.RIGHTEL.chargeName
            if (isTaliaChargeCode.get())
                return ChargeOrganization.TALIA.chargeName
        }

        return ""
    }

    private fun calcChargeAmountCode(amount: String): String{
        val num = amount[0]
        var zeroNumbers = 0
        amount.forEach {
            if (it == '0')
                zeroNumbers++
        }
        return zeroNumbers.toString() + num
    }

}
