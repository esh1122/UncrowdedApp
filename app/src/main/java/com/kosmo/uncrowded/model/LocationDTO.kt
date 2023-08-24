package com.kosmo.uncrowded.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationDTO(
    val location_poi: String = "",
    val location_name: String = "",
    val location_type: String = "",
    val location_latitude: Double = 0.0,
    val location_longitude: Double = 0.0,
    val gu_code: Int = 0,
    val location_description: String = "",
    val location_inter_code: String = "",
    // DB에 이미지 넣을때 사용하는 필드
    val location_image_base: ByteArray? = null,
    // DB에서 이미지 불러올때 사용하는 필드
    val location_image_string: String = "",
    // DB에 안넣지만 연산에만 사용하는 것
    val recommendScore: Int = 0,
    // Gu 테이블 정보
    val gu_name: String = "",
    val gu_dongs: String = "",
    // location_inter 테이블 정보
    val location_inter_name: String = "",
    // SKT API에서 받아오는 혼잡도 정보
    val datetime: String = "",
    val congestion: Double = 0.0,  // m2당 사람 수
    val congest_level_skt: String = "",
    // RTD API에서 받아오는 혼잡도 정보
    var congest_level: String = "",
    var max_people: Int = 0,
    var min_people: Int = 0,
    var resident_rate: Double? = null,
//    var forecastList: List<ForecastDTO>? = null,
    var air_idx: String = "",
    var air_idx_mvl: Double = 0.0,
    var precipitation: String = "",
    var sunset_time: String = "",
    var temperature: Double = 0.0,
    var uv_index: String = "",
    var sensible_temp: String = "",
    var code: String = "",

    val nx: String = "",
    val ny: String = ""
)

