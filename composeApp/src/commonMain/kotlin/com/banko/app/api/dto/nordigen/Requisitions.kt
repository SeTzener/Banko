package com.banko.app.api.dto.nordigen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Requisitions(
    val id: String,
    val created: String,
    val redirect: String,
    val status: String,
    @SerialName("institution_id")
    val institutionId: String,
    val agreement: String,
    val reference: String,
    val accounts: List<String>,
    @SerialName("user_language")
    val userLanguage: String,
    val link: String,
    val ssn: String?,
    @SerialName("account_selection")
    val accountSelection: Boolean,
    @SerialName("redirect_immediate")
    val redirectImmediate: Boolean,
)