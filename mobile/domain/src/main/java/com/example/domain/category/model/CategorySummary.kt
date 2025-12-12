package com.example.domain.category.model

data class CategorySummary(
    val categoryId: Int,
    val categoryName: String,
    val categoryType: String,
    val totalAmount: Double,
    val transactionCount: Int
)
