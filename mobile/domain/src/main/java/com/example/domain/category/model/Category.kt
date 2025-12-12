package com.example.domain.category.model

data class Category(
    val id: Int,
    val name: String,
    val type: String, // "income", "expense", "transfer"
    val color: String?,
    val icon: String?,
    val userId: Int?
)

