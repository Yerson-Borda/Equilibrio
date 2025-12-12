package com.example.domain.category.model

data class CategoryCreateRequest(
    val name: String,
    val type: String, // "income", "expense", "transfer"
    val color: String? = null,
    val icon: String? = null
)