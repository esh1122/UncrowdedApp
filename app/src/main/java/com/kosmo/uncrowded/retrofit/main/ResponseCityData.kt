package com.kosmo.uncrowded.retrofit.main
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseCityData(
    @SerialName("SeoulRtd.citydata_ppltn")
    val SeoulRtd: List<AreaInfo>
)

@Serializable
data class AreaInfo(
    val AREA_NM: String,
    val AREA_CD: String,
    var AREA_CONGEST_LVL: String,
    val AREA_CONGEST_MSG: String,
    val AREA_PPLTN_MIN: String,
    val AREA_PPLTN_MAX: String,
    val MALE_PPLTN_RATE: String,
    val FEMALE_PPLTN_RATE: String,
    val PPLTN_RATE_0: String,
    val PPLTN_RATE_10: String,
    val PPLTN_RATE_20: String,
    val PPLTN_RATE_30: String,
    val PPLTN_RATE_40: String,
    val PPLTN_RATE_50: String,
    val PPLTN_RATE_60: String,
    val PPLTN_RATE_70: String,
    val RESNT_PPLTN_RATE: String,
    val NON_RESNT_PPLTN_RATE: String,
    val REPLACE_YN: String,
    val PPLTN_TIME: String,
    val FCST_YN: String,
    val FCST_PPLTN: List<ForecastInfo>? = null
)

@Serializable
data class ForecastInfo(
    val FCST_TIME: String,
    val FCST_CONGEST_LVL: String,
    val FCST_PPLTN_MIN: String,
    val FCST_PPLTN_MAX: String
)
