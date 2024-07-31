package com.topkishmopix.peak.setting.shop

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fanap.corepos.database.base.IUserRepository
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.utils.Utils
import com.fanap.corepos.utils.aryan.AryanUtils
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentShiftBinding
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.setting.shop.viewmodel.CodeRequestViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val EDIT_PREF_TEXT = " com.masa.aryan.setting.edit"

@AndroidEntryPoint
class ShiftFragment : BaseFragment<FragmentShiftBinding>() {
 @Inject
 lateinit var sharedpref: SharedPreferences

 @Inject
 lateinit var appContext: Context

 private val viewModel: CodeRequestViewModel by viewModels()
 private val userRepository: IUserRepository by lazy {
  DependencyManager.provideUserRepository(
   appContext
  )
 }


 override fun getViewBinding(
  inflater: LayoutInflater,
  container: ViewGroup?
 ) = FragmentShiftBinding.inflate(inflater, container, false)

 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)

  binding.viewModel = viewModel

//  Gone the View in first view
  binding.disableShift.visibility = View.GONE

// how to show the ui to user if user is enable what see and if is disable what to show
  lifecycleScope.launch {

   try {
    if (userRepository.getEnabledUser(true)!!.userId == sharedpref.getString(EDIT_PREF_TEXT, "")!!
      .toLong()
    ) {
     binding.enableShift.visibility = View.VISIBLE
     binding.getCode.visibility = View.VISIBLE
     binding.disableShift.visibility = View.GONE
     binding.codeTxt.text = sharedpref.getString(EDIT_PREF_TEXT, "")

    } else if (userRepository.getEnabledUser(false)!!.isEnabled == true)

     binding.enableShift.visibility = View.GONE
    binding.getCode.visibility = View.GONE
    binding.disableShift.visibility = View.VISIBLE
    binding.enableShift.visibility = View.GONE

    val txt = sharedpref.getString(EDIT_PREF_TEXT, "")!!.toLong()
    if (userRepository.getValue(txt)!!.isEnabled)
     binding.nameCode.text = userRepository.getValue(txt)!!.name

   } catch (e: Exception) {
   }
  }


  viewModel.onError.observe(viewLifecycleOwner) {
   Utils.makeSnack(binding.root, it, Snackbar.LENGTH_SHORT).show()

  }

//  for enable button
  viewModel.onSuccess.observe(viewLifecycleOwner) {

   sharedpref.edit().apply {
    putString(EDIT_PREF_TEXT, binding.code.text.toString())
   }.apply()

   binding.codeTxt.text = sharedpref.getString(EDIT_PREF_TEXT, "")

   lifecycleScope.launch(Dispatchers.IO) {
    withContext(Dispatchers.Main) {

     userRepository.getShiftByUserId(
      binding.codeTxt.text.toString().toLong(),
      AryanUtils.getPersianDate()
     )
     userRepository.updateStatusUserForStart(
      start = AryanUtils.getPersianDate(),
      true,
      false,
      binding.codeTxt.text.toString().toLong()
     )

    }

    Utils.makeSnack(
     binding.root,
     "  در تاریخ : ${AryanUtils.getPersianDate()} کاربر: ${
      userRepository.getValue(
       binding.codeTxt.text.toString().toLong()
      )!!.name
     } فعال شد",
     Snackbar.LENGTH_SHORT
    )
     .show()
    val txt =  sharedpref.getString(EDIT_PREF_TEXT,"")!!.toLong()
    try{
     if(userRepository.getValue(txt)!!.isEnabled)
      binding.nameCode.text =  userRepository.getValue(txt)!!.name
    }catch(e:Exception){ }
   }
   binding.currentUser.visibility = View.VISIBLE
binding.nameCode.visibility = View.VISIBLE
   binding.getCode.visibility = View.GONE
   binding.disableShift.visibility = View.VISIBLE
   binding.enableShift.visibility = View.GONE



  }

//  for disable button
  binding.disableShift.setOnClickListener {
   lifecycleScope.launch(Dispatchers.IO) {
    withContext(Dispatchers.Main) {
     userRepository.getShiftByUserId(
      binding.codeTxt.text.toString().toLong(),
      AryanUtils.getPersianDate()
     )
     userRepository.updateStatusUserForEnd(
      end = AryanUtils.getPersianDate(),
      false,
      true,
      binding.codeTxt.text.toString().toLong()
     )
    }
    Utils.makeSnack(
     binding.root,
     "  در تاریخ : ${AryanUtils.getPersianDate()} کاربر: ${
      userRepository.getValue(
       binding.codeTxt.text.toString().toLong()
      )!!.name
     } غیر فعال شد",
     Snackbar.LENGTH_SHORT
    )
     .show()

    delay(500)
    binding.codeTxt.text = ""
   }

   binding.codeTxt.visibility = View.GONE
   binding.currentUser.visibility = View.GONE
   binding.enableShift.visibility = View.VISIBLE
   binding.getCode.visibility = View.VISIBLE
   binding.disableShift.visibility = View.GONE
   binding.nameCode.visibility = View.GONE

  }


  binding.back.setOnClickListener {
   navigate(
    this,
    R.id.action_shiftFragment_to_swipeFragment
   )
  }

  onBackPressed.observe(viewLifecycleOwner) {
   navigate(
    this,
    R.id.action_shiftFragment_to_swipeFragment
   )
  }

  binding.exit.setOnClickListener {
   navigate(
    this,
    R.id.action_shiftFragment_to_swipeFragment
   )

  }

 }

}

