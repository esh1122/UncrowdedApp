package com.kosmo.uncrowded.model

import com.kosmo.uncrowded.model.event.EventDTO
import kotlinx.serialization.Serializable

@Serializable
data class LocationDetailDTO(
    val area_ppltn_avg: Int = 0,
    val precpt_type: String = "",
    val air_idx: String = "",
    val events: List<EventDTO>? = null
)
