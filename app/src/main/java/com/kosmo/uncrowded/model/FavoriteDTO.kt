package com.kosmo.uncrowded.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteDTO(val email : String = "", val location_poi : Int = -1)
