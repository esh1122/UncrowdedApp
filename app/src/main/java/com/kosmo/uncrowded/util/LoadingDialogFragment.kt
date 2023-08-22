package com.kosmo.uncrowded.util

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentLoadingLayoutBinding


class LoadingDialogFragment: DialogFragment() {

    private var binding : FragmentLoadingLayoutBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("TimeMeasurement","LoadingDialogFragment onCreateView")
        binding = FragmentLoadingLayoutBinding.inflate(inflater,container,false)
        isCancelable = false
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.LoadingDialogTheme)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("TimeMeasurement","LoadingDialogFragment destroy")
        binding?.let {
            binding = null
        }

    }
}