package com.example.domain.categoryLimit.model

data class CategoryLimitOverview(
    val categoryId: Int,
    val categoryName: String,
    val categoryColor: String?,
    val categoryIcon: String?,
    val monthlyLimit: String,
    val monthlySpent: String
)
