package com.topkishmopix.peak.charge

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentChargeAmountBinding

class ChargeAmountFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentChargeAmountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChargeAmountBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val isTxt1Visible = requireArguments().getBoolean("1000")
            binding.txt1.visibility = if (isTxt1Visible) View.VISIBLE else View.GONE
        } catch (ignored: Exception) {
        }

        binding.txt1.setOnClickListener { sendAmount(10000) }
        binding.txt2.setOnClickListener { sendAmount(20000) }
        binding.txt3.setOnClickListener { sendAmount(50000) }
        binding.txt4.setOnClickListener { sendAmount(100000) }
        binding.txt5.setOnClickListener { sendAmount(200000) }
        binding.txt6.setOnClickListener { sendAmount(500000) }

        binding.txtTitle.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun sendAmount(i: Int) {
        setFragmentResult("200", bundleOf("Amount" to i.toString()))
        findNavController().popBackStack()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialog1: DialogInterface ->
            val d = dialog1 as BottomSheetDialog
            val bottomSheet = d.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet!!)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        return dialog
    }
}