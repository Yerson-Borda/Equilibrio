package com.example.domain.transaction.model

data class TransactionEntity(
    val id: Int,
    val name: String, // Changed from 'amount' to 'name' (this might be a display name/title)
    val amount: String,
    val note: String?, // Keep note
    val type: String, // "income", "expense", "transfer"
    val transactionDate: String,
    val walletId: Int,
    val categoryId: Int,
    val userId: Int,
    val createdAt: String,
    val tags: List<String> = emptyList() // Added tags field
)