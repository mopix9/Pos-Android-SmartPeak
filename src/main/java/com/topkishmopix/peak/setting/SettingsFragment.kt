package com.topkishmopix.peak.setting

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentSettingsBinding
import com.topkishmopix.peak.base.BaseFragment
import javax.inject.Inject

const val EDIT_PREF_TEXT = " com.masa.aryan.setting.edit"
class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    @Inject
    lateinit var appContext: Context




    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.management.setOnClickListener {
            navigate(
             this,
             R.id.action_settingsFragment_to_passwordFragment,
             bundleOf(
                 "IsTerminalPass" to false,
                 "Destination" to R.id.action_passwordFragment_to_managementFragment
             )
            )
        }

        binding.shop.setOnClickListener {
            navigate(
             this,
             R.id.action_settingsFragment_to_passwordFragment,
             bundleOf(
                 "IsTerminalPass" to true,
                 "Destination" to R.id.action_passwordFragment_to_shopFragment
             )
            )
        }

        binding.buyer.setOnClickListener {
            navigate(
             this,
             R.id.action_settingsFragment_to_buyerFragment
            )
        }

        binding.shift.setOnClickListener {

            navigate(
             this,
             R.id.action_settingsFragment_to_shiftFragment )

        }

        binding.back.setOnClickListener {
            navigate(
             this,
             R.id.action_settingsFragment_to_swipeFragment
            )
        }

        onBackPressed.observe(viewLifecycleOwner) {

            navigate(
             this,
             R.id.action_settingsFragment_to_swipeFragment
            )
        }
    }
}



