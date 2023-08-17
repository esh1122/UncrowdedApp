package com.kosmo.uncrowded.view

import android.graphics.Color
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kosmo.uncrowded.MainActivity
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentDetailLocationBinding
import com.kosmo.uncrowded.model.FavoriteDTO
import com.kosmo.uncrowded.model.LocationDTO
import com.kosmo.uncrowded.model.LocationDetailDTO
import com.kosmo.uncrowded.model.ResponseResultSuccess
import com.kosmo.uncrowded.model.event.EventRecyclerViewAdapter
import com.kosmo.uncrowded.model.event.EventRecyclerViewDecoration
import com.kosmo.uncrowded.retrofit.location.LocationService
import com.kosmo.uncrowded.retrofit.member.MemberService
import com.squareup.picasso.Picasso
import io.multimoon.colorful.ColorfulColor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class DetailLocationFragment : Fragment() {

    private var binding: FragmentDetailLocationBinding? = null
    private val args: DetailLocationFragmentArgs by navArgs()
    private var memberFavorites : MutableList<FavoriteDTO>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailLocationBinding.inflate(inflater,container,false)
        val location = Json.decodeFromString<LocationDTO>(args.locationPoi)
        memberFavorites = (requireActivity() as MainActivity).fragmentMember.favorites

        binding?.let {binding->
            var isChecked =
                if(! memberFavorites.isNullOrEmpty()){
                    memberFavorites!!.any { it.location_poi == location.location_poi }
                }else{
                    false
                }
            if(isChecked) binding.locationBookmark.setImageResource(R.drawable.clicked_bookmark)

            Picasso.get().load(location.location_image_string).into(binding.detailLocationImage)
            binding.detailLocationName.text = location.location_name

            when(location.congest_level){
                "여유" -> binding.bgCrowdLvl.setBackgroundColor(Color.GREEN)
                "보통" -> binding.bgCrowdLvl.setBackgroundColor(Color.YELLOW)
                "약간 붐빔" -> binding.bgCrowdLvl.setBackgroundColor(Color.parseColor("#ffd700"))
                "붐빔" -> binding.bgCrowdLvl.setBackgroundColor(Color.RED)
            }
            binding.textCrowdLvl.text = location.congest_level

            binding.locationBookmark.setOnClickListener {
                updateFavorite((requireActivity() as MainActivity).fragmentMember.email,location.location_poi){
                    val dialog = AlertDialog.Builder(this.requireContext())
                        .setTitle("즐겨찾기 변경")
                    if(it.isSuccess){
                        dialog.setMessage(it.message).show()
                        if (isChecked){
                            binding.locationBookmark.setImageResource(R.drawable.unclicked_bookmark)
                            Firebase.messaging.unsubscribeFromTopic("locationPOI${location.location_poi}")
                        }else{
                            binding.locationBookmark.setImageResource(R.drawable.clicked_bookmark)
                            Firebase.messaging.subscribeToTopic("locationPOI${location.location_poi}")
                        }
//                        (requireActivity() as MainActivity).setMember(){
//
//                        }
                        isChecked = !isChecked
                    }else{
                        dialog.setMessage(it.message).show()
                    }
                }
            }

            getDetailData(location.location_poi){
                val weatherImage = when{
                    it.sky in 3..4-> R.drawable.ic_cloud
                    it.pty in 2..3 -> R.drawable.ic_snow
                    it.pty == 1 -> R.drawable.ic_rain
                    else -> R.drawable.ic_brightness
                }
                binding.locationWeather.setImageResource(weatherImage)
                binding.realtimePopulation.text = "${it.area_ppltn_avg}"
                binding.particulates.text = it.air_idx

                it.events?.let { events->
                    val adapter = EventRecyclerViewAdapter(this,events)
                    val linearLayoutManager = LinearLayoutManager(this.activity, RecyclerView.HORIZONTAL,false)
                    linearLayoutManager.isSmoothScrollbarEnabled = false
                    binding.locationDetailRecyclerView.adapter = adapter
                    binding.locationDetailRecyclerView.addItemDecoration(EventRecyclerViewDecoration(60,0))
                    binding.locationDetailRecyclerView.layoutManager = linearLayoutManager
                }
            }
        }
        return binding?.root
    }

    private fun updateFavorite(email:String,locationPoi:String,callback:(ResponseResultSuccess) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.login_fast_api)) // Kakao API base URL
            .addConverterFactory(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .build()
        val service = retrofit.create(MemberService::class.java)
        val call = service.updateMemberFavorite(FavoriteDTO(email,locationPoi))
        call.enqueue(object : Callback<ResponseResultSuccess>{
            override fun onResponse(
                call: Call<ResponseResultSuccess>,
                response: Response<ResponseResultSuccess>
            ) {
                val responseResult = response.body() ?: return
                callback(responseResult)
            }

            override fun onFailure(call: Call<ResponseResultSuccess>, t: Throwable) {
                Log.i("DetailLocation","전송 실패 : ${t.message}")
            }

        })
    }

    private fun getDetailData(location_poi: String,callback:(LocationDetailDTO)->Unit){
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.login_fast_api)) // Kakao API base URL
            .addConverterFactory(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .build()
        val service = retrofit.create(LocationService::class.java)
        val call = service.getLocationDetail(location_poi)
        call.enqueue(object : Callback<LocationDetailDTO>{
            override fun onResponse(
                call: Call<LocationDetailDTO>,
                response: Response<LocationDetailDTO>
            ) {
                response.body()?.let { callback(it) }
            }

            override fun onFailure(call: Call<LocationDetailDTO>, t: Throwable) {
                Log.i("DetailLocationFragment","전송 실패 : ${t.message}")
            }

        })
    }

}