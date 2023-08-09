package com.kosmo.uncrowded.retrofit.main

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface MapService {
    @GET("/{key}/json/citydata_ppltn/1/1/{searchLocation}")
    fun getRTD(
        @Path("key") key: String,
        @Path("searchLocation") searchLocation:String
    ) : Call<ResponseCityData?>
}