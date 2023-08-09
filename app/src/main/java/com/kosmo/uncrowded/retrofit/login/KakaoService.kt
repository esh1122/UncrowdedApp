package com.kosmo.uncrowded.retrofit.login

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface KakaoService {
    @GET("v2/user/me")
    fun getUserInfo(@Header("Authorization") auth: String): Call<MutableMap<String,Any>>
}