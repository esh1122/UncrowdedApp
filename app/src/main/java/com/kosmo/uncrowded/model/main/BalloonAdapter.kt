package com.kosmo.uncrowded.model.main

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.CustomCalloutBalloonBinding
import com.kosmo.uncrowded.databinding.KakaoMapBalloonLayoutBinding
import com.kosmo.uncrowded.model.LocationDTO
import com.kosmo.uncrowded.retrofit.main.AreaInfo
import com.squareup.picasso.Picasso
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem


class BalloonAdapter(
    val fragment : Fragment,
    private val locations : List<LocationDTO>
): CalloutBalloonAdapter  {

    private val binding : CustomCalloutBalloonBinding = CustomCalloutBalloonBinding.inflate(fragment.layoutInflater)


    override fun getCalloutBalloon(p0: MapPOIItem?): View? {
        p0?.let {p0->
            val areaInfo = p0.userObject as AreaInfo
            val location = locations.find { "POI${it.location_poi}" == areaInfo.AREA_CD}
            location?.let {
                it.congest_level = areaInfo.AREA_CONGEST_LVL
                p0.userObject = it
                Picasso.get()
                    .load(location.location_image_string)
                    .error(R.drawable.close)
                    .into(binding.locationImage)
                binding.locationName.text = "${p0.itemName}"
                binding.locationLevel.text = "${it.congest_level}"
            }
        }
        return binding.root
    }

    override fun getPressedCalloutBalloon(p0: MapPOIItem?): View? {
        return null
    }
}