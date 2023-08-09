package com.kosmo.uncrowded.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.navArgs
import com.kosmo.uncrowded.MainActivity
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentDetailLocationBinding
import com.kosmo.uncrowded.model.LocationDTO
import com.kosmo.uncrowded.model.MemberDTO
import kotlinx.serialization.json.Json

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class DetailLocationFragment : Fragment() {

    private var binding: FragmentDetailLocationBinding? = null
    private val args: DetailLocationFragmentArgs by navArgs()
    private lateinit var member : MemberDTO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailLocationBinding.inflate(inflater,container,false)
        val location = Json.decodeFromString<LocationDTO>(args.locationPoi)
        member = (requireActivity() as MainActivity).fragmentMember

        binding?.let {binding->
            val isChecked =
                if(! member.favorites.isNullOrEmpty()){
                    member.favorites!!.any { it.location_poi == location.location_poi }
                }else{
                    false
                }
            if(isChecked) binding.locationBookmark.setImageResource(R.drawable.clicked_bookmark)

            binding.locationBookmark.setOnClickListener {
//                (it as ImageView).setImageResource()
            }

        }

        return binding?.root
    }
}