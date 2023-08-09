package com.kosmo.uncrowded.model.json

import kotlinx.serialization.Serializable

@Serializable
data class Feature(
    val type: String,
    val geometry: Geometry,
    val properties: Properties,
    val center: List<Double>
)

@Serializable
data class Geometry(
    val type: String,
    val coordinates: List<List<List<Double>>>
)

@Serializable
data class Properties(
    val CATEGORY: String,
    val AREA_CD: String,
    val AREA_NM: String
)