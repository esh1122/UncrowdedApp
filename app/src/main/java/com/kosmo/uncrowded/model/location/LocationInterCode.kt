package com.kosmo.uncrowded.model.location

import com.kosmo.uncrowded.R

enum class LocationInterCode(val interCode: String, val drawableId: Int, val krName: String) {
    LANDMARK("L001",R.drawable.ic_landmark,"관광특구"),
    CULTURAL_HERITAGE("L002",R.drawable.ic_cultural_heritage,"문화유산"),
    PLAZA("L003",R.drawable.ic_cultural_facilities,"인구밀집지역"),
    STORE("L004",R.drawable.ic_store,"발달상권"),
    PARK("L005",R.drawable.ic_park,"공원"),
    TRAIN("L006",R.drawable.ic_train,"교통"),
    HOSPITAL("L007",R.drawable.ic_hospital,"병원"),
    CULTURAL_FACILITIES("L008",R.drawable.ic_cultural_facilities,"문화시설"),
    SPORTS_FACILITY("L009",R.drawable.ic_sports_facility,"체육시설"),
    ETC("L010",R.drawable.ic_etc,"기타");

    companion object {
        fun fromInterCode(interCode: String): LocationInterCode? {
            return values().find { it.interCode == interCode }
        }
    }
}