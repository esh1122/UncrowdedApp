package com.kosmo.uncrowded.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kosmo.uncrowded.MainActivity
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.FragmentEventBinding
import com.kosmo.uncrowded.model.event.EventDTO
import com.kosmo.uncrowded.model.event.EventRecyclerViewAdapter
import com.kosmo.uncrowded.model.event.EventRecyclerViewDecoration
import com.kosmo.uncrowded.model.event.EventSelectorAdapter
import com.kosmo.uncrowded.model.event.EventSortMenuCode
import com.kosmo.uncrowded.retrofit.event.EventService
import com.kosmo.uncrowded.util.LoadingDialogFragment
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.GridHolder
import kotlinx.serialization.json.Json
import nl.joery.animatedbottombar.AnimatedBottomBar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


class EventFragment : Fragment() {

    private lateinit var context : Context
    private var binding: FragmentEventBinding? = null
    private lateinit var dialog : DialogPlus
    private val spinnerItems = EventSortMenuCode.values()
//    private lateinit var fusedLocationClient : FusedLocationProviderClient
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("com.kosmo.uncrowded","EventFragment 생성")
        binding = FragmentEventBinding.inflate(inflater,container,false)
        val adapter = EventSelectorAdapter(context, false, spinnerItems.size)

        getRecommendByLocation()

        binding?.let { binding ->
            dialog = DialogPlus.newDialog(context).apply {
                setContentHolder(GridHolder(3))
                isCancelable = true
                setGravity(Gravity.BOTTOM)
                setHeader(R.layout.spinner_header)
                setAdapter(adapter)
                setOnItemClickListener { dialog, item, view, position ->
                    binding.eventSpinner.text = spinnerItems[position].target
                    if(position == 0){
                        getRecommendByLocation()
                    }else{
                        getEvent(spinnerItems[position].name.lowercase())
                    }
                    dialog.dismiss()
                }
                setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            }.create()

            binding.eventSpinner.text = spinnerItems[0].target
            binding.eventSpinner.setOnClickListener { v->
                dialog.show()
            }

            binding.searchEvent.setOnEditorActionListener{ v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getSearchedEvent()
                    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(binding.searchEvent.windowToken, 0)
                    v.clearFocus()
                    true
                } else {
                    false
                }
            }
        }

        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        val activity = activity as AppCompatActivity?
        if (activity != null) {
            val bottomBar = activity.findViewById<AnimatedBottomBar>(R.id.bottom_bar)
            bottomBar?.selectTabAt(2)
        }
    }

    private fun getRecommendByLocation(){
        Log.i("eventFragment","권한 확인 시작")
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Log.i("eventFragment","권한 확인")
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = LocationManager.FUSED_PROVIDER
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                // 위치를 사용한 작업 수행
                getEvent(spinnerItems[0].name.lowercase(),latitude,longitude)
                // 업데이트 중지
                locationManager.removeUpdates(this)
            }
        }
        locationManager.requestLocationUpdates(provider, 1000, 1f, locationListener)
    }

    private fun getEvent(requirement : String, lat: Double= 0.0, lng: Double= 0.0 ){
        val loadingDialog = LoadingDialogFragment()
        loadingDialog.show(requireActivity().supportFragmentManager, "LoadingDialogFragment")
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.login_fast_api)) // Kakao API base URL
            .addConverterFactory(Json{
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .client(OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)  // 연결 타임아웃
                .readTimeout(1, TimeUnit.MINUTES)     // 데이터 읽기 타임아웃
                .writeTimeout(1, TimeUnit.MINUTES)    // 데이터 쓰기 타임아웃
                .build())
            .build() //스프링 REST API로 회원여부 판단을 위한 요청
        val service = retrofit.create(EventService::class.java)
        val call = if(requirement == spinnerItems[0].name.lowercase()){
            service.getRecommendEvents((requireActivity() as MainActivity).member.value!!.email,lat, lng)
        }else{
            service.getEvents(requirement)
        }
        call.enqueue(object : Callback<MutableList<EventDTO>>{
            override fun onResponse(
                call: Call<MutableList<EventDTO>>,
                response: Response<MutableList<EventDTO>>
            ) {
                val events = response.body()!!
                val adapter = EventRecyclerViewAdapter(this@EventFragment,events)
                Log.i("eventFragment","전송 성공 ${events.size}")
                binding?.let { binding->
                    binding.eventList.adapter = adapter
                    if(binding.eventList.itemDecorationCount == 0){
                        binding.eventList.addItemDecoration(EventRecyclerViewDecoration(0,60))
                    }
                    binding.eventList.layoutManager = LinearLayoutManager(this@EventFragment.activity, RecyclerView.VERTICAL,false)
                    loadingDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<MutableList<EventDTO>>, t: Throwable) {
                Log.i("com.kosmo.uncrowded.event","eventDto 전송 실패 ${t.message}")
            }

        })
    }

    private fun getSearchedEvent(){
        val loadingDialog = LoadingDialogFragment()
        loadingDialog.show(requireActivity().supportFragmentManager, "LoadingDialogFragment")
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.login_fast_api)) // Kakao API base URL
            .addConverterFactory(Json{
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .build() //스프링 REST API로 회원여부 판단을 위한 요청
        binding?.let { binding ->
            val service = retrofit.create(EventService::class.java)
            val call = service.getSearchedEvents(binding.searchEvent.text.toString())
            call.enqueue(object : Callback<MutableList<EventDTO>?> {
                override fun onResponse(
                    call: Call<MutableList<EventDTO>?>,
                    response: Response<MutableList<EventDTO>?>
                ) {
//                    Log.i("event", "event:${response.body()}")
                    val events = response.body() ?: return AlertDialog.Builder(context)
                        .setTitle("이벤트를 찾을 수 없습니다").setCancelable(true).create().show()
                    val adapter = EventRecyclerViewAdapter(this@EventFragment, events)
                    binding.eventList.adapter = adapter
                    binding.eventList.addItemDecoration(EventRecyclerViewDecoration(0,60))
                    binding.eventList.layoutManager = LinearLayoutManager(
                        this@EventFragment.activity,
                        RecyclerView.VERTICAL,
                        false
                    )
                    loadingDialog.dismiss()
                }

                override fun onFailure(call: Call<MutableList<EventDTO>?>, t: Throwable) {
                    Log.i("com.kosmo.uncrowded.event", "eventDto 전송 실패 ${t.message}")
                }
            })
        }
    }
}