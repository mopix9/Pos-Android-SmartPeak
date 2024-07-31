package com.topkishmopix.peak.setting.management

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.database.base.ISettingsRepository
import com.fanap.corepos.database.service.model.Settings
import com.fanap.corepos.database.service.model.SettingsNames
import com.fanap.corepos.di.DependencyManager
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentConnectivityBinding
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.utils.SSLInitiator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ConnectivityFragment : BaseFragment<FragmentConnectivityBinding>() {

    @Inject
    lateinit var appContext: Context

    private val settingsRepository: ISettingsRepository by lazy {
        DependencyManager.provideSettingsRepository(appContext)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentConnectivityBinding.inflate(inflater,container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.server.setOnClickListener {
            navigate(
             this,
             R.id.action_connectivityFragment_to_serverFragment
            )
        }

        lifecycleScope.launch {
            val checked = settingsRepository.getValue(SettingsNames.SSL.name)?.value.toBoolean()
            binding.swSsl.isChecked = checked
        }

        binding.swSsl.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch(Dispatchers.IO) {
                settingsRepository.insert(Settings(SettingsNames.SSL.name,isChecked.toString()))
            }
            SSLInitiator().init(isChecked,appContext)
        }

        binding.btnGprs.setOnClickListener{
            val intent = Intent()
            intent.component = ComponentName(
                "com.android.settings",
                "com.android.settings.Settings\$DataUsageSummaryActivity"
            )
            startActivity(intent)
        }

        binding.btnWifi.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
        }

        binding.back.setOnClickListener {
            finish(this)
        }

        onBackPressed.observe(viewLifecycleOwner,{
            finish(this)
        })
    }

}