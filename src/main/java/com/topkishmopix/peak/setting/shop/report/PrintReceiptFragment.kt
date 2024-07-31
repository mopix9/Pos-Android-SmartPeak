package com.topkishmopix.peak.setting.shop.report

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.database.base.ITransactionRepository
import com.fanap.corepos.device.DeviceSDKManager
import com.fanap.corepos.device.print.PrinterInterface
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.utils.Utils
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentPrintReceiptBinding
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.main.view.view.LoadingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PrintReceiptFragment : BaseFragment<FragmentPrintReceiptBinding>() {

    @Inject
    lateinit var loading : LoadingFragment
    @Inject
    lateinit var appContext: Context
    private val transactionRepository: ITransactionRepository by lazy {
        DependencyManager.provideTransactionRepository(
            appContext
        )
    }

    private val printer: PrinterInterface? by lazy {
        DeviceSDKManager.getPrinterInterface(appContext)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPrintReceiptBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lastReceipt.setOnClickListener {
            lifecycleScope.launch {
                val transaction = transactionRepository.getLastPrintableTransaction()
                if (transaction != null) {
                    navigate(
                     this@PrintReceiptFragment,
                     R.id.action_printReceiptFragment_to_showReceiptFragment,
                     bundleOf("ID" to transaction.id)
                    )
                } else {
                    Utils.makeSnack(binding.root,"تراکنشی یافت نشد!",Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.searchReceipt.setOnClickListener {
            navigate(
             this,
             R.id.action_printReceiptFragment_to_searchReceiptFragment
            )
        }

        binding.back.setOnClickListener {
            finish(this)
        }

        onBackPressed.observe(viewLifecycleOwner,{
            finish(this)
        })
    }


}