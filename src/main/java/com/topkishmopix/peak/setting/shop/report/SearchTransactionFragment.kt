package com.topkishmopix.peak.setting.shop.report

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fanap.corepos.database.base.ITransactionRepository
import com.fanap.corepos.database.service.model.Transaction
import com.fanap.corepos.di.DependencyManager
import com.fanap.corepos.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.topkishmopix.peak.R
import com.topkishmopix.peak.databinding.FragmentSearchTransactionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val BY_STAN = 0
private const val BY_RRN = 1

@AndroidEntryPoint
class SearchTransactionFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSearchTransactionBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var appContext: Context
    private val transactionRepository: ITransactionRepository by lazy {
        DependencyManager.provideTransactionRepository(
            appContext
        )
    }

    private var transaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchTransactionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (requireArguments().getInt("TYPE")) {
            BY_RRN -> {
                binding.txtTitle.text = "بر اساس شماره مرجع"
                binding.edtInput.hint = "شماره مرجع تراکنش را وارد نمائید"
            }
            BY_STAN -> {
                binding.txtTitle.text = "بر اساس شماره پیگیری"
                binding.edtInput.hint = "شماره پیگیری تراکنش را وارد نمائید"
            }
            else -> {
                dismiss()
            }
        }

        binding.btnSearch.setOnClickListener {
            if (binding.edtInput.text.isNotEmpty()) {
                lifecycleScope.launch {

                    transaction = when (requireArguments().getInt("TYPE")) {
                        BY_RRN -> {
                              transactionRepository.getTransactionByRrn(binding.edtInput.text.toString())
                        }
                        BY_STAN -> {
                              transactionRepository.getTransactionByStan(binding.edtInput.text.toString())
                        }
                        else -> null
                    }

                    if (transaction != null)
                        send(transaction!!.id)
                    else {
                        Utils.makeSnack(binding.root, "تراکنشی یافت نشد!", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            } else
                Utils.makeSnack(binding.root, "لطفا مقدار معتبر وارد نمائید!", Snackbar.LENGTH_SHORT).show()
        }

    }

    private fun send(id: Long) {
        findNavController().navigate(R.id.action_searchTransactionFragment_to_showReceiptFragment,
            bundleOf("ID" to id))
        dismiss()
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