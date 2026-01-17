package com.example.domain.categoryLimit.model

data class CategoryLimit(
    val id: Int,
    val categoryId: Int,
    val userId: Int,
    val monthlyLimit: String
)