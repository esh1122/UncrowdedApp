package com.kosmo.uncrowded.retrofit.picture

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PictureService {
    @POST("crowdcount")
    fun getCnnPeopleCount(@Body params: Map<String, String>): Call<ResponseCnn>
}