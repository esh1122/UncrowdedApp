package com.kosmo.uncrowded.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentLocationBinding
import com.kosmo.uncrowded.model.event.EventRecyclerViewDecoration
import com.kosmo.uncrowded.model.location.LocationInterCode
import com.kosmo.uncrowded.model.location.LocationItemAdapter
import com.kosmo.uncrowded.model.location.LocationSelectorAdapter
import nl.joery.animatedbottombar.AnimatedBottomBar

class LocationFragment : Fragment() {

    private var binding: FragmentLocationBinding? = null
    private var selectorIndex: Int = -1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("com.kosmo.uncrowded","LocationFragment 생성")
        binding = FragmentLocationBinding.inflate(layoutInflater)

        binding?.let { binding->
            val adapter = LocationSelectorAdapter(this){locations->
                Log.i("LocationFragment","locations size : ${locations.size}")
                if(locations.isEmpty()){
                    AlertDialog.Builder(requireContext())
                        .setTitle("현재 준비중인 페이지입니다")
                        .setMessage("현재 개발중인 페이지입니다.\n이용에 불편을 드려서 죄송합니다")
                        .show()
                    return@LocationSelectorAdapter
                }
                val index = LocationInterCode.fromInterCode(locations[0].location_inter_code)?.ordinal ?: -1
                if(selectorIndex != index){
                    binding.locationItemRecyclerview.adapter = LocationItemAdapter(this,locations)
                    if(binding.locationItemRecyclerview.itemDecorationCount == 0){
                        binding.locationItemRecyclerview.addItemDecoration(EventRecyclerViewDecoration(0,30))
                    }
                    binding.locationItemRecyclerview.layoutManager = LinearLayoutManager(
                        this.activity,
                        RecyclerView.VERTICAL,
                        false
                    )
                }
            }
            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(binding.locationSelectorRecyclerview)
            binding.locationSelectorRecyclerview.adapter = adapter
            binding.locationSelectorRecyclerview.addItemDecoration(EventRecyclerViewDecoration(30,0))
            binding.locationSelectorRecyclerview.layoutManager = LinearLayoutManager(
                this.activity,
                RecyclerView.HORIZONTAL,
                false
            )
        }
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        val activity = activity as AppCompatActivity?
        if (activity != null) {
            val bottomBar = activity.findViewById<AnimatedBottomBar>(R.id.bottom_bar)
            bottomBar?.selectTabAt(1)
        }
    }
}