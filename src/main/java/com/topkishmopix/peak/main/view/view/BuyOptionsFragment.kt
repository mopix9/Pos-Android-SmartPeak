package com.topkishmopix.peak.main.view.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentBuyOptionsBinding
import com.topkishmopix.peak.base.BaseFragment


class BuyOptionsFragment : BaseFragment<FragmentBuyOptionsBinding>() {


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBuyOptionsBinding {
        return FragmentBuyOptionsBinding.inflate(inflater,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startTimer(10000)
        onTimerFinish.observe(viewLifecycleOwner,{
            finish(this)
        })
        onBackPressed.observe(viewLifecycleOwner,{
            finish(this)
        })


        binding.lnrBuy.setOnClickListener {
            navigate(
                this,
                R.id.action_buyOptionsFragment_to_buyFragment,
                arguments
            )
        }

        binding.lnrBalance.setOnClickListener {
            navigate(
                this,
                R.id.action_buyOptionsFragment_to_balanceFragment,
                arguments)

        }

        binding.lnrBill.setOnClickListener {
            navigate(
                this,
                R.id.action_buyOptionsFragment_to_billFragment,
                arguments
            )
        }


        binding.lnrCharge.setOnClickListener {
            navigate(this,R.id.action_buyOptionsFragment_to_chargeFragment, arguments)
        }



        binding.back.setOnClickListener {
//            finish(this)
            navigate(this,R.id.action_buyOptionsFragment_to_swipeFragment)
        }

        onBackPressed.observe(viewLifecycleOwner) {

            //            finish(this)
            navigate(this,R.id.action_buyOptionsFragment_to_swipeFragment)
        }
    }

}