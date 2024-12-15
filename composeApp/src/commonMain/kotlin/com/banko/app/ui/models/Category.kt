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
    val amount: Float,
    val color: Color
)

// TODO(): Temporary
val categories = listOf(
    Category(name = "E.Shop", amount = 2000f, color = DarkMidnightBlue),
    Category(name = "Share Market", amount = 1500f, color = Orange),
    Category(name = "Sports", amount = 500f, color = PatriarchPurple),
    Category(name = "Export", amount = 1352f, color = SteelBlue),
    Category(name = "Culo", amount = 1752f, color = Teal),
    Category(name = "Cacca", amount = 1352f, color = Firebrick),
    Category(name = "Export", amount = 1352f, color = Coral),
    Category(name = "E.Shop", amount = 2000f, color = MediumPurple),
    Category(name = "Share Market", amount = 1500f, color = DarkTurquoise),
    Category(name = "Sports", amount = 500f, color = Olive),
)