package com.kosmo.uncrowded.retrofit.location

import kotlinx.serialization.Serializable

@Serializable
data class ResponseWeather(
    val response: ResponseBody
)
@Serializable
data class ResponseBody(
    val header: Header,
    val body: Body
)
@Serializable
data class Header(
    val resultCode: String,
    val resultMsg: String
)
@Serializable
data class Body(
    val dataType: String,
    val items: Items,
    val pageNo: Int,
    val numOfRows: Int,
    val totalCount: Int
)
@Serializable
data class Items(
    val item: List<Item>
)
@Serializable
data class Item(
    val baseDate: String,
    val baseTime: String,
    val category: String,
    val fcstDate: String,
    val fcstTime: String,
    val fcstValue: String,
    val nx: Int,
    val ny: Int
)