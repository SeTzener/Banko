package com.banko.app.api.dto.bankoApi

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String? = null,
    val consentGiven: Boolean
)

@Serializable
data class AuthResponse(
    val accountId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)
