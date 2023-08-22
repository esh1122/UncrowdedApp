package com.kosmo.uncrowded.retrofit.login

import com.kosmo.uncrowded.model.MemberDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {

    @POST("/users/login")
    fun login(
        @Body member: MemberDTO
    ): Call<MemberDTO?>

    @POST("/users/login")
    fun loginFromKakao(
        @Body member: MemberDTO
    ): Call<MemberDTO?>

    @POST("/token")
    fun getUncrowdedToken(
        @Body member: MemberDTO
    ): Call<Map<String,String>>

}