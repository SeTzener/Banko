package com.banko.app.api.dto.catFacts

import kotlinx.serialization.Serializable

@Serializable
data class CatFact(
    val fact: String
)