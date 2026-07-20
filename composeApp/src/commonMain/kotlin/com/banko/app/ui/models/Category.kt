package com.banko.app.ui.models

import androidx.compose.ui.graphics.Color

data class Category(
    val id: String?,
    val name: String,
    val amount: Double,
    val color: Color
)