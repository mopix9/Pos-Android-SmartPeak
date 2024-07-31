package com.topkishmopix.peak.bill

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.iso.IIso
import com.fanap.corepos.iso.utils.IsoFields
import com.fanap.corepos.utils.Utils
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.R
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.bill.viewmodel.BillViewModel
import com.topkishmopix.peak.databinding.FragmentBillBinding
import com.topkishmopix.peak.setting.shop.EDIT_PREF_TEXT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val SCANNER_REQUEST = "100"
@AndroidEntryPoint
class BillFragment : BaseFragment<FragmentBillBinding>() {

    @Inject
    lateinit var share: SharedPreferences

    private val transactionManager: IIso by lazy { DependencyManager.provideIsoTransaction() }
    private val viewModel : BillViewModel by viewModels()
    private lateinit var track2 : String

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBillBinding {
        return FragmentBillBinding.inflate(inflater,container,false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        startTimer()

        track2 = arguments?.getString("Track2","") ?: ""

        onTimerFinish.observe(viewLifecycleOwner,{
            navigate(
                this,
                R.id.action_billFragment_to_swipeFragment
            )
        })
        onBackPressed.observe(viewLifecycleOwner,{
            navigate(
                this,
                R.id.action_billFragment_to_swipeFragment
            )
        })
        onTouchListener.observe(viewLifecycleOwner,{ startTimer() })
        binding.edtBillId.addTextChangedListener { startTimer() }
        binding.edtPayId.addTextChangedListener { startTimer() }

        binding.back.setOnClickListener { navigate(
            this,
            R.id.action_billFragment_to_swipeFragment
        ) }
        binding.cancel.setOnClickListener { navigate(
            this,
            R.id.action_billFragment_to_swipeFragment
        ) }
        binding.scanner.setOnClickListener {
            navigate(
                this,
                R.id.action_billFragment_to_scannerFragment
            )
        }
        viewModel.onConfirmClicked.observe(viewLifecycleOwner,{
            billInquiry()
        })

        viewModel.onError.observe(viewLifecycleOwner,{
            Utils.makeSnack(binding.root,it, Snackbar.LENGTH_LONG).show()
        })


        setFragmentResultListener(SCANNER_REQUEST) { _, bundle ->
            val result = bundle.getString("data")
            try {
                viewModel.billId.set(result!!.substring(0, 13))
                viewModel.payId.set(result.substring(13, 26))
            } catch (e: Exception) {
                Utils.makeSnack(binding.root, "قبض نامعتبر!", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun billInquiry(){
        lifecycleScope.launch (Dispatchers.IO) {
            delay(500)
            withContext(Dispatchers.Main) {
                stopTimer()
            }

            val map = viewModel.makeTransaction()
            val result = transactionManager.doTransaction(map)

            withContext(Dispatchers.Main) {
                if (result != null) {
                    val responseCode = result[IsoFields.Response] ?: ""
                    if (responseCode == "94") {
                        Utils.makeSnack(binding.root,"قبض قبلا پرداخت شده است!",Snackbar.LENGTH_LONG).show()
                        finish(this@BillFragment)
                    }else{
                        navigate(
                            this@BillFragment,
                            R.id.action_billFragment_to_billDetailFragment,
                            bundleOf("BillId" to viewModel.billId.get(),
                                "PayId" to viewModel.payId.get(), "Amount" to viewModel.amount.get(), "Track2" to track2 , "UID" to share.getString(EDIT_PREF_TEXT,"")!!.toLong())
                        )
                    }
                } else {
                    navigate(
                        this@BillFragment,
                        R.id.action_billFragment_to_billDetailFragment,
                        bundleOf("BillId" to viewModel.billId.get(),
                            "PayId" to viewModel.payId.get(), "Amount" to viewModel.amount.get(), "Track2" to track2 , "UID" to share.getString(EDIT_PREF_TEXT,"")!!.toLong())
                     )
                }
            }
        }
    }
}