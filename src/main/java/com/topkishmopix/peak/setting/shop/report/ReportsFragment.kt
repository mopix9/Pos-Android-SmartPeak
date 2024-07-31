package com.topkishmopix.peak.setting.shop.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentReportsBinding
import com.topkishmopix.peak.base.BaseFragment


class ReportsFragment : BaseFragment<FragmentReportsBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReportsBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.total.setOnClickListener {
            navigate(
             this,
             R.id.action_reportsFragment_to_totalReportFragment
            )
        }
        binding.totalUserId.setOnClickListener {
            navigate(
             this,
             R.id.action_reportsFragment_to_totalReportByUserIdFragment
            )
        }

        binding.reportDetails.setOnClickListener {
            navigate(
             this,
             R.id.action_reportsFragment_to_transactionDetailsFragment
            )
        }

        binding.printReceipt.setOnClickListener {
            navigate(
             this,
             R.id.action_reportsFragment_to_printReceiptFragment
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