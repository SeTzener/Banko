package com.banko.app.ui.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.banko.app.api.utils.jsonAdapters.ColorSerializer
import com.banko.app.domain.model.ExpenseTag as DomainExpenseTag
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

fun ExpenseTag.toDomain() = DomainExpenseTag(
    id = id,
    name = name,
    color = color.toArgb().toLong(),
    isEarning = isEarning,
    aka = aka
)