package com.kosmo.uncrowded.model.event

import kotlinx.serialization.Serializable

@Serializable
data class EventDTO(
    val event_code : String = "",
    val event_title : String = "",
    val event_image_url :String = "",
    val event_startdate : String = "",
    val event_enddate : String = "",
    val event_venue_name : String = "",
    val event_venue_latitude : Float = 0F ,
    val event_venue_longitude : Float = 0F ,
    val event_theme : String = "",
    val event_seated : String = "",
    val event_isfree : String = "",
    val event_webpage : String = "",
    val gu_code : Int = 0,
    val inter_code : String = "",
    val event_org_name : String = "",
    val event_use_target : String = "",
    val event_player : String = "",
    val event_use_fee : String = "",
    val event_description : String = "",
    val event_time : String = "",

    val inter_name : String = "",

    val more_ifo : String = "",
    val recommendscore : Int = 0
)
