package com.grove.app.data.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
data class User(val name: String = "Mae", val resetDay: Int = 1, val currency: String = "USD")

@Immutable
data class Category(val id: String, val name: String, val budget: Double)

@Immutable
data class Expense(
    val id: String,
    val amount: Double,
    val category: String,
    val note: String,
    val date: LocalDateTime,
)

@Immutable
data class Bill(
    val id: String,
    val name: String,
    val amount: Double,
    val dueDay: Int,
    val paid: Boolean,
    val icon: String,
)
