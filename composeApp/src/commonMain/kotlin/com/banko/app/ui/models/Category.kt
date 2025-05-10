package com.banko.app.ui.models

import androidx.compose.ui.graphics.Color
import com.banko.app.ui.theme.Coral
import com.banko.app.ui.theme.DarkMidnightBlue
import com.banko.app.ui.theme.DarkTurquoise
import com.banko.app.ui.theme.Firebrick
import com.banko.app.ui.theme.MediumPurple
import com.banko.app.ui.theme.Olive
import com.banko.app.ui.theme.Orange
import com.banko.app.ui.theme.PatriarchPurple
import com.banko.app.ui.theme.SteelBlue
import com.banko.app.ui.theme.Teal

data class Category(
    val name: String,
    val amount: Double,
    val color: Color
)