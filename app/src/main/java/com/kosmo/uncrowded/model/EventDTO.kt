package com.kosmo.uncrowded.model

data class EventDTO(
    var event_Code : String,
    val event_Title : String = "",
    val event_Image_Url :String = "",
    val event_StartDate : java.util.Date? = null,
    val event_EndDate : java.util.Date? = null,
    val event_Venue_Name : String = "",
    val event_Venue_Latitude : Double? = null ,
    val event_Venue_Longitude : Double? = null ,
    val event_Theme : String = "",
    val event_Seated : Char? = null,
    val event_IsFree : Char? = null,
    val event_Webpage : String = "",
    val gu_code : String = "",
    val inter_Code : String = "",
    val event_org_name : String = "",
    val event_use_target : String = "",
    val event_player : String = "",
    val event_use_fee : String = "",
    val event_description : String = "",
    val event_time : String = "",

    val inter_name : String = "",

    val more_ifo : String = "",
    val recommendScore : Int = 0
)
