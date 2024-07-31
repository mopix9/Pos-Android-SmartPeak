package com.topkishmopix.peak.setting.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentShopBinding
import com.topkishmopix.peak.base.BaseFragment

class ShopFragment : BaseFragment<FragmentShopBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentShopBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reports.setOnClickListener {
            navigate(
             this,
             R.id.action_shopFragment_to_reportsFragment
            )
        }

        binding.rollRequest.setOnClickListener {
            navigate(
             this,
             R.id.action_shopFragment_to_sellerRollRequestFragment
            )
        }

        binding.back.setOnClickListener {
            navigate(
             this,
             R.id.action_shopFragment_to_settingsFragment
            )
        }

        binding.createUser.setOnClickListener {
            navigate(
             this,
             R.id.action_shopFragment_to_createUserFragment
            )
        }

        onBackPressed.observe(viewLifecycleOwner,{
            navigate(
             this,
             R.id.action_shopFragment_to_settingsFragment
            )
        })
    }

}