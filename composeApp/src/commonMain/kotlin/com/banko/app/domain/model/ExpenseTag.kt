package com.banko.app.domain.model

data class ExpenseTag(
    val id: String,
    val name: String,
    val color: Long,
    val isEarning: Boolean?,
    val aka: List<String>
)
