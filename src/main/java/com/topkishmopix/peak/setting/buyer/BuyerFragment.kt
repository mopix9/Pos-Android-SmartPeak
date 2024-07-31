package com.topkishmopix.peak.setting.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentBuyerBinding
import com.topkishmopix.peak.base.BaseFragment


class BuyerFragment : BaseFragment<FragmentBuyerBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentBuyerBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settlement.setOnClickListener {
            navigate(
             this,
             R.id.action_buyerFragment_to_settlementFragment
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