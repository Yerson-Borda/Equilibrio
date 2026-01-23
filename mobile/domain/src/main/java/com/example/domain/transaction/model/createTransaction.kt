package com.example.domain.transaction.model

import java.io.File

data class CreateTransaction(
    val name: String, // Added name parameter
    val amount: Any, // Can be Number or String
    val note: String?,
    val type: String, // "income", "expense", "transfer"
    val transactionDate: String,
    val walletId: Int,
    val categoryId: Int,
    val tags: List<Int> = emptyList(), // Added tags parameter
    val receiptFile: File? = null // Receipt image file
)