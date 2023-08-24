package com.kosmo.uncrowded.model.location

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentLocationBinding
import com.kosmo.uncrowded.databinding.SimpleGridItemBinding
import com.kosmo.uncrowded.model.LocationDTO
import com.kosmo.uncrowded.retrofit.location.LocationService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LocationSelectorAdapter(
    val fragment : Fragment,
    private val callback : (List<LocationDTO>)->Unit
): RecyclerView.Adapter<LocationSelectorAdapter.LocationSelectorViewHolder>() {

    private val locationEnum = LocationInterCode.values()
    private var selectedPosition = -1

    inner class LocationSelectorViewHolder(binding: SimpleGridItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemName: TextView = binding.textView
        val itemPoster: ImageView = binding.imageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationSelectorViewHolder {
        val binding = SimpleGridItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return LocationSelectorViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return locationEnum.size
    }

    override fun onBindViewHolder(holder: LocationSelectorViewHolder, position: Int) {
        val data = locationEnum[position]
        holder.itemName.text = data.krName
        holder.itemPoster.setImageResource(data.drawableId)
        (holder.itemName.parent as View).setOnClickListener {
            if(selectedPosition != data.ordinal){
                getLocationsByInterCode(data,callback)

            }
        }
    }

    private fun getLocationsByInterCode(data:LocationInterCode, callback:(List<LocationDTO>)-> Unit){
        val retrofit = Retrofit.Builder()
            .baseUrl(fragment.resources.getString(R.string.login_fast_api))
            .addConverterFactory(Json{
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .build() //스프링 REST API로 회원여부 판단을 위한 요청
        val service = retrofit.create(LocationService::class.java)
        val call = service.getLocationsByInterCode(data.interCode)
        call.enqueue(object : Callback<List<LocationDTO>>{
            override fun onResponse(
                call: Call<List<LocationDTO>>,
                response: Response<List<LocationDTO>>
            ) {
                response.body()?.let {
                    callback(it)
                    if (it.isNotEmpty()){
                        selectedPosition = data.ordinal
                    }
                }
            }

            override fun onFailure(call: Call<List<LocationDTO>>, t: Throwable) {
                Log.i("LocationFragment","전송 실패 : ${t.message}")
            }

        })
    }
}