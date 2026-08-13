package com.banko.app.data.mapper

import com.banko.app.domain.model.ExpenseTag as DomainExpenseTag
import com.banko.app.database.Entities.ExpenseTag as DaoExpenseTag
import com.banko.app.api.dto.bankoApi.ExpenseTag as DtoExpenseTag

fun DaoExpenseTag.toDomain() = DomainExpenseTag(
    id = id,
    name = name,
    color = color,
    isEarning = isEarning,
    aka = aka ?: emptyList()
)

fun DtoExpenseTag.toDomain() = DomainExpenseTag(
    id = id,
    name = name,
    color = color,
    isEarning = isEarning,
    aka = aka ?: emptyList()
)

fun DomainExpenseTag.toDao() = DaoExpenseTag(
    id = id,
    name = name,
    color = color,
    isEarning = isEarning,
    aka = if (aka.isEmpty()) null else aka
)

fun DomainExpenseTag.toDto() = DtoExpenseTag(
    id = id,
    name = name,
    color = color,
    isEarning = isEarning,
    aka = if (aka.isEmpty()) null else aka
)
