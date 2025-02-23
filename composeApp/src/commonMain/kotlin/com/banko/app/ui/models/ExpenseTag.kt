package com.banko.app.ui.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.banko.app.api.utils.jsonAdapters.ColorSerializer
import kotlinx.serialization.Serializable

typealias DtoExpenseTag = com.banko.app.api.dto.bankoApi.ExpenseTag
typealias DaoExpenseTag = com.banko.app.database.Entities.ExpenseTag

@Serializable
data class ExpenseTag (
    val id: String,
    val name: String,
    @Serializable(with = ColorSerializer::class)
    val color: Color,
    val aka: List<String>
)

fun ExpenseTag.toDto() = DtoExpenseTag(
    id = id,
    name = name,
    color = color.toArgb().toLong(),
    aka = if (aka.isEmpty()) null else aka
)

fun ExpenseTag.toDao() = DaoExpenseTag(
    id = id,
    name = name,
    color = color.toArgb().toLong(),
    aka = if (aka.isEmpty()) null else aka
)