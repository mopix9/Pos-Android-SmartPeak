package com.topkishmopix.peak.setting.shop.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentSearchReceiptBinding
import com.topkishmopix.peak.base.BaseFragment


class SearchReceiptFragment : BaseFragment<FragmentSearchReceiptBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSearchReceiptBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchByRrn.setOnClickListener {
            navigate(
             this,
             R.id.action_searchReceiptFragment_to_searchTransactionFragment,
             bundleOf("TYPE" to 1)
            )
        }

        binding.searchByStan.setOnClickListener {
            navigate(
             this,
             R.id.action_searchReceiptFragment_to_searchTransactionFragment,
             bundleOf("TYPE" to 0)
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