package com.kosmo.uncrowded.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentMainBinding
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MainFragment : Fragment(), MapView.MapViewEventListener {

    private var binding : FragmentMainBinding? = null
    private lateinit var mapView : MapView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("com.kosmo.uncrowded","MainFragment 생성")
        binding = FragmentMainBinding.inflate(inflater,container,false)

        val mapView = MapView(requireActivity())
        val mapViewContainer = binding!!.mapView as ViewGroup
        mapViewContainer.addView(mapView)
        mapView.setMapViewEventListener(this)

        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onPause()
        binding = null
    }

    override fun onMapViewInitialized(mapView: MapView?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

    }

}