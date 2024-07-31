package com.topkishmopix.peak.setting.buyer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.database.base.ITransactionRepository
import com.fanap.corepos.database.service.model.TransactionStatus
import com.fanap.corepos.di.DependencyManager
import com.topkishmopix.peak.databinding.FragmentSettlementBinding
import com.topkishmopix.peak.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class SettlementFragment : BaseFragment<FragmentSettlementBinding>() {

    @Inject
    lateinit var appContext : Context
    private val transactionRepository: ITransactionRepository by lazy { DependencyManager.provideTransactionRepository(appContext) }


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSettlementBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startTimer()

        onTimerFinish.observe(viewLifecycleOwner,{
            finish(this)
        })
        onBackPressed.observe(viewLifecycleOwner,{
            finish(this)
        })

        onTouchListener.observe(viewLifecycleOwner,{
            startTimer()
        })

        binding.back.setOnClickListener {
            finish(this)
        }

        lifecycleScope.launch (Dispatchers.Main){
            var msg = ""
            delay(200)
            withContext(Dispatchers.IO){
                val lastTransaction = transactionRepository.getLastBuyTransaction()
                if (lastTransaction?.response == "00" && lastTransaction.status != TransactionStatus.AdviceResUnpacked.name){
                    lastTransaction.status = TransactionStatus.AdviceResUnpacked.name
                    transactionRepository.updateTransaction(lastTransaction)
                    msg = "عملیات با موفقیت انجام شد."

                }else if(lastTransaction?.response == "-1" && lastTransaction.status != TransactionStatus.ReverseResUnpacked.name){
                    lastTransaction.status = TransactionStatus.ReverseResUnpacked.name
                    transactionRepository.updateTransaction(lastTransaction)
                    msg = "عملیات با موفقیت انجام شد."
                }else
                    msg = "تراکنش ناقص یافت نشد."
            }
            binding.txtResult.text = msg
            binding.loading.visibility = View.GONE
            delay(1500)
            finish(this@SettlementFragment)
        }
    }

}
