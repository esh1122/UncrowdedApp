package com.kosmo.uncrowded.retrofit.member

import com.kosmo.uncrowded.model.FavoriteDTO
import com.kosmo.uncrowded.model.ResponseResultSuccess
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

interface MemberService {

    @PATCH("/member/favorite/")
    fun updateMemberFavorite(@Body favoriteDTO:FavoriteDTO): Call<ResponseResultSuccess>

}