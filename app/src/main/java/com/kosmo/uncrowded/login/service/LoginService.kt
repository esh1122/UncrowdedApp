package com.kosmo.uncrowded.login.service

import com.kosmo.uncrowded.model.MemberDTO
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginService {
    @FormUrlEncoded
    @POST("/users/login")
    fun isLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<MemberDTO?>
}