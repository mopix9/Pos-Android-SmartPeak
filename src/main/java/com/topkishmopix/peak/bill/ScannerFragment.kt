package com.topkishmopix.peak.bill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.zxing.Result
import com.topkishmopix.peak.databinding.FragmentScannerBinding
import com.topkishmopix.peak.base.BaseFragment
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScannerFragment  : BaseFragment<FragmentScannerBinding>(),
    ZXingScannerView.ResultHandler{
    private var mScannerView: ZXingScannerView? = null
    private var mFlash = false
    private var mAutoFocus = true
    private var mCameraId = -1
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        mScannerView = ZXingScannerView(activity)
//        return mScannerView
//    }

    override fun onResume() {
        super.onResume()
        mScannerView?.setResultHandler(this)
        mScannerView?.startCamera(mCameraId)
        mScannerView?.flash = mFlash
        mScannerView?.setAutoFocus(mAutoFocus)
    }

    override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera()
    }

    override fun handleResult(rawResult: Result?) {
        setFragmentResult("100", bundleOf("data" to rawResult?.text))
        finish(this)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentScannerBinding {
        return FragmentScannerBinding.inflate(inflater,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startTimer(30_000)

        mScannerView = binding.scanner

        binding.close.setOnClickListener{
            finish(this)
        }

        onTimerFinish.observe(viewLifecycleOwner,{
            finish(this)
        })
        onBackPressed.observe(viewLifecycleOwner,{
            finish(this)
        })

    }
}