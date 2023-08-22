package com.kosmo.uncrowded.retrofit.picture

import kotlinx.serialization.Serializable

@Serializable
data class ResponseCnn(
    val prediction: Int = 0,
    val base64Plot: String = ""
)
