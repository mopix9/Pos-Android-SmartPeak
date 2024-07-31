package com.topkishmopix.peak.base

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.topkishmopix.peak.main.view.MainActivity
import kotlinx.coroutines.Job
import java.util.concurrent.TimeUnit

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    var timer: CountDownTimer? = null
        private set

    var onTimerTick = MutableLiveData<String>()
    var onTimerFinish = MutableLiveData<Boolean>()
    var onBackPressed = MutableLiveData<Boolean>()
    var onTouchListener = MutableLiveData<Boolean>()
    lateinit var uiStateJob : Job
    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed.postValue(true)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = getViewBinding(inflater, container)

        (activity as MainActivity).onTouchEvent.observe(viewLifecycleOwner,{
            onTouchListener.postValue(true)
        })

        return binding.root
    }

    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onDestroyView() {
        timer?.cancel()
        super.onDestroyView()
    }

    fun startTimer(time: Long = 60000) {
        timer?.cancel()
        timer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.e("CountDownTimer", "BaseFragment :" + TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toString())
                onTimerTick.postValue(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toString())
            }

            override fun onFinish() {
                timer?.cancel()
                onTimerFinish.postValue(true)
            }
        }
        (timer as CountDownTimer).start()
    }



    fun stopTimer() = timer?.cancel()

    fun navigate(fragment: Fragment, destination: Int, bundle: Bundle? = null){
        val currentFragment = try {
            (findNavController().currentDestination as FragmentNavigator.Destination).className.split('.').last()

        }catch (e : Exception){
            (findNavController().currentDestination as DialogFragmentNavigator.Destination).className.split('.').last()
        }

        if (currentFragment == fragment.javaClass.simpleName)
            findNavController().navigate(destination,bundle)
    }




    fun finish(fragment : Fragment){
        findNavController().popBackStack()
    }

}
