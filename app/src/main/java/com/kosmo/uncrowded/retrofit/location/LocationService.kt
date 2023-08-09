package com.kosmo.uncrowded.retrofit.location

import com.kosmo.uncrowded.model.LocationDTO
import retrofit2.Call
import retrofit2.http.GET

interface LocationService {
    @GET("/locations")
    fun getLocations() :Call<List<LocationDTO>>
}