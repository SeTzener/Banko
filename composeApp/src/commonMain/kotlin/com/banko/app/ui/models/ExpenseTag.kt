package com.banko.app.ui.models

import androidx.compose.ui.graphics.Color

data class ExpenseTag (
    val id: String,
    val name: String,
    val color: Color,
    val aka: List<String>
)