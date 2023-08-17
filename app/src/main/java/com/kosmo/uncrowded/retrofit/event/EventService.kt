package com.kosmo.uncrowded.retrofit.event

import com.kosmo.uncrowded.model.event.EventDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventService {
    @GET("/events/{requirement}")
    fun getEvents(@Path("requirement") requirement: String): Call<MutableList<EventDTO>>
    @GET("/events/search")
    fun getSearchedEvents(@Query("search_word") searchWord: String): Call<MutableList<EventDTO>?>

    @GET("/events/recommend")
    fun getRecommendEvents(@Query("email") email: String,@Query("lat") lat: Double,@Query("lng") lng: Double): Call<MutableList<EventDTO>>
}