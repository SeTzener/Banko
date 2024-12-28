package com.banko.app.api.dto.nordigen

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val access: String,
    val refresh: String,
    val access_expires: String,
    val refresh_expires: String
)