package com.kosmo.uncrowded.view

import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kosmo.uncrowded.R
import com.kosmo.uncrowded.databinding.CustomCalloutBalloonBinding
import com.kosmo.uncrowded.databinding.FragmentMainBinding
import com.kosmo.uncrowded.model.LocationDTO
import com.kosmo.uncrowded.model.json.Feature
import com.kosmo.uncrowded.retrofit.location.LocationService
import com.kosmo.uncrowded.retrofit.main.AreaInfo
import com.kosmo.uncrowded.retrofit.main.MapService
import com.kosmo.uncrowded.retrofit.main.ResponseCityData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapCircle
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import nl.joery.animatedbottombar.AnimatedBottomBar
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException


class MainFragment : Fragment(), MapView.MapViewEventListener, MapView.POIItemEventListener {
    private var binding : FragmentMainBinding? = null
    private lateinit var mapView : MapView
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener: LocationListener
    private var isListening = false
    private val circles = mutableListOf<MapCircle>()
    private val markers = mutableListOf<MapPOIItem>()
    private var locations = listOf<LocationDTO>()
    private var container: ViewGroup? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("com.kosmo.uncrowded","MainFragment 생성")
        binding = FragmentMainBinding.inflate(inflater,container,false)
        this.container = container
        mapView = MapView(requireActivity())

        getLocations {
            Log.i("fastApi","LocationDTOs : ${it?.size}")
            it?.let {
//                mapView.setCalloutBalloonAdapter(BalloonAdapter(this,it))
                locations = it
                mapView.setCalloutBalloonAdapter(BalloonAdapter())
            }
        }
        locationManager = activity?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        locationListener = LocationListener {
            Log.i("MainFragment","지도 페이지 갱신")
            val lat = it.latitude
            val lng = it.longitude
            mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat, lng), true)
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER,3000L,5F,locationListener)
        }catch (e : SecurityException){
            e.printStackTrace()
        }
        setPOI()

        val mapViewContainer = binding!!.mapView as ViewGroup
        mapViewContainer.addView(mapView)
        return binding!!.root
    }

    override fun onStart() {
        super.onStart()
        val activity = activity as AppCompatActivity?
        if (activity != null) {
            val bottomBar = activity.findViewById<AnimatedBottomBar>(R.id.bottom_bar)
            bottomBar?.selectTabAt(0)
        }
    }

    override fun onResume() {
        super.onResume()
        if(!isListening){
            mapView.setMapViewEventListener(this)
        }
        mapView.setPOIItemEventListener(this)
    }

    override fun onPause() {
        Log.i("MainFragment","MainFragment onDestroyView 호출")
        super.onPause()
    }

    override fun onDestroyView() {
        Log.i("MainFragment","MainFragment onDestroyView 호출")
        super.onDestroyView()
        (binding?.mapView as ViewGroup).removeAllViews()
        binding = null
        if (isListening) {
            locationManager.removeUpdates(locationListener)
            isListening = false
        }
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

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {

    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {

    }

    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {
        Log.i("Main.Balloon","Balloon 리스너 호출")
        p1?.let { p1->
            val jsonString = Json.encodeToString(p1.userObject as LocationDTO)
            val action = MainFragmentDirections.actionMainFragmentToDetailLocationFragment(jsonString)
            // NavController를 통해 ReceiverFragment로 이동하면서 데이터를 전송합니다.
            NavHostFragment.findNavController(this@MainFragment).navigate(action)
        }
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {

    }

    private fun setPOI(){
        val list = parseJsonFile()
        Log.i("MainFragment","list의 크기 : ${list?.size}")
        Log.i("MainFragment","mapView? : $mapView")
        list?.forEachIndexed  { index,feature ->
            getCrowdLvlFromApi(feature.properties.AREA_NM){responseCityData->
                val color : Int
//                Log.i("MainFragment","responseCityData : $responseCityData")
                responseCityData?.let {
                    val areaInfo = it.SeoulRtd[0]
                    when(areaInfo.AREA_CONGEST_LVL){
                        "붐빔" ->{
                            color = Color.parseColor("#80FF0000")
                        }
                        "약간 붐빔" ->{
                            color = Color.parseColor("#80FF8000")
                        }
                        "보통"->{
                            color = Color.parseColor("#80deec10")
                        }
                        else ->{
                            color = Color.parseColor("#8000FF00")
                        }
                    }
                    val circle = makeCircle(feature.center[1],feature.center[0],color,index) //0부터 태그 시작
                    circles.add(circle)

                    val marker = makeMarker(feature.center[1],feature.center[0],feature.properties.AREA_NM,areaInfo,index) //1000부터 태그 시작
                    markers.add(marker)

                    mapView.addPOIItem(marker)
                    mapView.addCircle(circle)
                }
            }
        }
    }

    private fun parseJsonFile(): List<Feature>? {
        val jsonText: String
        try {
            val inputStream = requireContext().assets.open("json/seoulRTD_light.json")
            jsonText = inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return try {
            Json.decodeFromString<List<Feature>>(jsonText)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCrowdLvlFromApi(locationName : String,callback: (ResponseCityData?) -> Unit){
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.seoul_url)) // Kakao API base URL
            .addConverterFactory(Json{
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .build()
        val service = retrofit.create(MapService::class.java)
        val call = service.getRTD(resources.getString(R.string.seoul_or_openapi_key),locationName)
        call.enqueue(object : Callback<ResponseCityData?>{
            override fun onResponse(
                call: Call<ResponseCityData?>,
                response: Response<ResponseCityData?>
            ) {
                callback(response.body())
            }
            override fun onFailure(call: Call<ResponseCityData?>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    private fun getLocations(callback: (List<LocationDTO>?) -> Unit){
        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.login_fast_api))
            .addConverterFactory(Json{
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.asConverterFactory("application/json".toMediaType()))
            .build()
        val service = retrofit.create(LocationService::class.java)
        val call = service.getLocations()
        call.enqueue(object : Callback<List<LocationDTO>>{
            override fun onResponse(
                call: Call<List<LocationDTO>>,
                response: Response<List<LocationDTO>>
            ) {
                callback(response.body())
            }

            override fun onFailure(call: Call<List<LocationDTO>>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }


    private fun makeCircle(lat: Double, lng: Double,fillColor:Int, tag: Int) : MapCircle{
        val circle = MapCircle(
            MapPoint.mapPointWithGeoCoord(lat, lng),  // center
            500,  // radius
            Color.BLACK,  // strokeColor
            fillColor // fillColor
        )
        circle.tag = tag
        return circle
    }

    private fun makeMarker(lat: Double, lng: Double, itemName: String,areaInfo: AreaInfo,  tag: Int) : MapPOIItem{
        val customMarker = MapPOIItem()
        customMarker.itemName = itemName
        customMarker.tag = 1000+tag
        customMarker.mapPoint = MapPoint.mapPointWithGeoCoord(lat, lng)
        customMarker.markerType = MapPOIItem.MarkerType.CustomImage // 마커타입을 커스텀 마커로 지정.
        customMarker.customImageResourceId = R.drawable.marker_star // 마커 이미지.
        customMarker.isCustomImageAutoscale = false
        customMarker.userObject = areaInfo
        customMarker.setCustomImageAnchor(
            0.5f,
            1.0f
        ) // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
        return customMarker
    }

    inner class BalloonAdapter(): CalloutBalloonAdapter  {

        private val binding : CustomCalloutBalloonBinding = CustomCalloutBalloonBinding.inflate(layoutInflater,container,false)
        var resultView: View? = null

        override fun getCalloutBalloon(p0: MapPOIItem?): View {
//            Picasso.Builder(this@MainFragment.requireContext()).build()
            val rootView = binding.root
            p0?.let { p0 ->
                val areaInfo = p0.userObject as AreaInfo
                val location = locations.find { "POI${it.location_poi}" == areaInfo.AREA_CD }
                location?.let {
                    it.congest_level = areaInfo.AREA_CONGEST_LVL
                    p0.userObject = it

//                    Glide.with(binding.root)
//                        .load(location.location_image_string)
//                        .into(binding.locationImage)
                    binding.locationName.text = "${p0.itemName}"
                    binding.locationLevel.text = "${location.congest_level}"

                }
            }
            Log.i("Main.Balloon","풍선 뷰 리턴")
            return rootView
        }

        override fun getPressedCalloutBalloon(p0: MapPOIItem?): View? {
            return null
        }

    }
}