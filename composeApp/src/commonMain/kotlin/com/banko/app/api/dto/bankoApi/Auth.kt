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

@Serializable
data class UpdateProfileRequest(
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class AcceptConsentRequest(
    val policyVersionId: String
)

@Serializable
data class UserProfileResponse(
    val accountId: String,
    val email: String,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val consentGiven: Boolean,
    val consentUpdatedAt: String? = null,
    val consentVersionId: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String? = null
)

@Serializable
data class ConsentLogEntry(
    val policyVersion: String,
    val policyTitle: String,
    val accepted: Boolean,
    val recordedAt: String
)

@Serializable
data class UserExportData(
    val accountId: String,
    val email: String,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val consentGiven: Boolean,
    val consentUpdatedAt: String? = null,
    val consentVersionId: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String? = null,
    val consentLogs: List<ConsentLogEntry>
)
