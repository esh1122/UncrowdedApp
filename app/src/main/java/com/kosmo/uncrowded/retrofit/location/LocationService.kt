package com.kosmo.uncrowded.retrofit.location

import com.kosmo.uncrowded.model.LocationDTO
import com.kosmo.uncrowded.model.LocationDetailDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LocationService {
    @GET("/locations")
    fun getLocations() :Call<List<LocationDTO>>

    @GET("/1360000/VilageFcstInfoService_2.0/getVilageFcst")
    fun getWeather(@Query("ServiceKey") serviceKey:String,
                   @Query("pageNo") pageNo:String= "1",
                   @Query("numOfRows") numOfRows:String = "12",
                   @Query("dataType") dataType:String = "12",
                   @Query("base_date") base_date:String,
                   @Query("base_time") base_time:String,
                   @Query("nx") nx:String,
                   @Query("ny") ny:String,): Call<ResponseWeather>

    @GET("/location/detail")
    fun getLocationDetail(@Query("location_poi") location_poi: String): Call<LocationDetailDTO>

    @GET("/locations/{inter_code}")
    fun getLocationsByInterCode(@Path("inter_code") inter_code: String): Call<List<LocationDTO>>
}