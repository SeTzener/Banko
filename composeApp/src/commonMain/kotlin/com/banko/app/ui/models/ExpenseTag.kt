package com.banko.app.ui.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.banko.app.DaoExpenseTag
import com.banko.app.DtoExpenseTag
import com.banko.app.api.utils.jsonAdapters.ColorSerializer
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseTag (
    val id: String,
    val name: String,
    @Serializable(with = ColorSerializer::class)
    val color: Color,
    val isEarning: Boolean?,
    val aka: List<String>
)

fun ExpenseTag.toDto() = DtoExpenseTag(
    id = id,
    name = name,
    color = color.toArgb().toLong(),
    isEarning = isEarning,
    aka = if (aka.isEmpty()) null else aka
)

fun ExpenseTag.toDao() = DaoExpenseTag(
    id = id,
    name = name,
    color = color.toArgb().toLong(),
    isEarning = isEarning,
    aka = if (aka.isEmpty()) null else aka
)