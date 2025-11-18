package com.example.domain.transaction.model

data class TransactionEntity(
    val id: Int,
    val amount: String,
    val note: String?,
    val type: String, // "income", "expense", "transfer"
    val transactionDate: String,
    val walletId: Int,
    val categoryId: Int,
    val userId: Int,
    val createdAt: String
)