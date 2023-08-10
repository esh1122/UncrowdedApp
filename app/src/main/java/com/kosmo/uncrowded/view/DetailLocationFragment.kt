package com.kosmo.uncrowded.view

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.navArgs
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kosmo.uncrowded.MainActivity
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentDetailLocationBinding
import com.kosmo.uncrowded.model.FavoriteDTO
import com.kosmo.uncrowded.model.LocationDTO
import com.kosmo.uncrowded.model.ResponseResultSuccess
import com.kosmo.uncrowded.retrofit.location.LocationService
import com.kosmo.uncrowded.retrofit.member.MemberService
import com.squareup.picasso.Picasso
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.time.format.DateTimeFormatter

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
            Log.i("DetailLocation","이미지 url:${location.location_image_string}")
            Picasso.get().load(location.location_image_string).into(binding.detailLocationImage)

            binding.locationBookmark.setOnClickListener {
                updateFavorite((requireActivity() as MainActivity).fragmentMember.email,location.location_poi){
                    val dialog = AlertDialog.Builder(this.requireContext())
                        .setTitle("즐겨찾기 변경")
                    if(it.isSuccess){
                        dialog.setMessage("즐겨찾기 변경에 성공했습니다").show()
                        binding.locationBookmark.setImageResource(if (isChecked) R.drawable.unclicked_bookmark else R.drawable.clicked_bookmark)
                        isChecked = false
                    }else{
                        dialog.setMessage("즐겨찾기 변경에 실패했습니다").show()
                    }
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
            .build() //스프링 REST API로 회원여부 판단을 위한 요청
        val service = retrofit.create(MemberService::class.java)
        val call = service.updateMemberFavorite(email,locationPoi)
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

    private fun getRealtimePopulation(){

    }

}