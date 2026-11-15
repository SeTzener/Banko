package com.banko.app.api.dto.bankoApi

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceRequest(
    val token: String,
    val platform: String
)

/**
 * The backend also returns the numeric platform enum. It is intentionally not
 * modelled here because the app never needs it and enum values are serialised
 * as numbers by the API.
 */
@Serializable
data class RegisterDeviceResponse(
    val id: String,
    val isActive: Boolean
)
