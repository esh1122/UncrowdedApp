package com.kosmo.uncrowded.retrofit.picture

import android.graphics.Bitmap
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PictureService {
    @POST("crowdcount")
    fun getCnnPeopleCount(@Body bitmap: String): Call<ResponseCnn>
}