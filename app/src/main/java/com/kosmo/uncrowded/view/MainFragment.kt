package com.kosmo.uncrowded.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentMainBinding
import net.daum.mf.map.api.MapView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MainFragment : Fragment() {

    private var binding : FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater,container,false)

        val mapView = MapView(requireActivity())
        val mapViewContainer = binding!!.mapView as ViewGroup
        mapViewContainer.addView(mapView)
        mapView.setMapViewEventListener {

        }
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}