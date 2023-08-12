package com.kosmo.uncrowded.model.location

import com.kosmo.uncrowded.R

enum class LocationInterCode(val interCode: String, val drawableId: Int) {
    LANDMARK("L001",R.drawable.ic_landmark),
    CULTURAL_HERITAGE("L002",R.drawable.ic_cultural_heritage),
    PLAZA("L003",R.drawable.ic_cultural_facilities),
    STORE("L004",R.drawable.ic_store),
    PARK("L005",R.drawable.ic_park),
    TRAIN("L006",R.drawable.ic_train),
    HOSPITAL("L007",R.drawable.ic_hospital),
    CULTURAL_FACILITIES("L008",R.drawable.ic_cultural_facilities),
    SPORTS_FACILITY("L009",R.drawable.ic_sports_facility),
    ETC("L010",R.drawable.ic_etc);

    companion object {
        fun fromInterCode(interCode: String): LocationInterCode? {
            return values().find { it.interCode == interCode }
        }
    }
}