package com.example.domain.wallet.model

data class Transaction(
    val id: Int,
    val amount: String,
    val description: String? = null,
    val note: String? = null,
    val type: String, // "income" or "expense"
    val date: String,
    val walletId: Int,
    val categoryId: Int,
    val userId: Int,
    val createdAt: String
) {
    // Helper property for UI
    val title: String
        get() = description ?: "Transaction"

    // Helper property for UI (you might want to map categoryId to category name)
    val category: String
        get() = when (categoryId) {
            // Add your category mappings here
            1 -> "Food & Drinks"
            2 -> "Shopping"
            3 -> "Transportation"
            4 -> "Entertainment"
            5 -> "Bills & Utilities"
            6 -> "Healthcare"
            7 -> "Income"
            else -> "Other"
        }
}