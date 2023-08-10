package com.kosmo.uncrowded.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseResultSuccess(
    val isSuccess: Boolean = false,
    val message: String = ""
)
