package com.banko.app.ui.models

import androidx.compose.ui.graphics.Color
import com.banko.app.api.utils.jsonAdapters.ColorSerializer
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseTag (
    val id: String,
    val name: String,
    @Serializable(with = ColorSerializer::class)
    val color: Color,
    val aka: List<String>
)