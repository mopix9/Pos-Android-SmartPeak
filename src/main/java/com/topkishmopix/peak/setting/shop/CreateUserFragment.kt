package com.topkishmopix.peak.setting.shop
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.fanap.corepos.utils.Utils
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.databinding.FragmentCreateUserBinding
import com.topkishmopix.peak.base.BaseFragment
import com.topkishmopix.peak.setting.shop.viewmodel.CreateUserViewModel

class CreateUserFragment : BaseFragment<FragmentCreateUserBinding>(){

 private val viewModel : CreateUserViewModel by viewModels()

 override fun getViewBinding(
  inflater: LayoutInflater,
  container: ViewGroup?
 ) =  FragmentCreateUserBinding.inflate(inflater,container,false)


 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)

  binding.viewModel = viewModel

  viewModel.onError.observe(viewLifecycleOwner) {
   Utils.makeSnack(binding.root, it, Snackbar.LENGTH_SHORT).show()
  }

  viewModel.onSuccess.observe(viewLifecycleOwner) {
   Utils.makeSnack(binding.root, "تنظیمات پایانه با موفقیت ذخیره شدند.", Snackbar.LENGTH_SHORT)
    .show()
   finish(this)
  }


  binding.exit.setOnClickListener {
   finish(this@CreateUserFragment)
  }

  binding.back.setOnClickListener {
   finish(this@CreateUserFragment)
  }

  onBackPressed.observe(viewLifecycleOwner) {
   finish(this@CreateUserFragment)
  }


 }
}