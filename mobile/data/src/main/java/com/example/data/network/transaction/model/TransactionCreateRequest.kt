package com.example.data.network.transaction.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionCreateRequest(
    @SerialName("amount") val amount: String, // number | string in swagger
    @SerialName("note") val note: String? = null, // Comes before type in swagger
    @SerialName("type") val type: String, // "income", "expense", "transfer"
    @SerialName("transaction_date") val transactionDate: String, // date format
    @SerialName("wallet_id") val walletId: Int,
    @SerialName("category_id") val categoryId: Int
    // Removed description field as it's not in swagger TransactionCreate
) {
    companion object {
        fun create(
            amount: Any,
            note: String? = null,
            type: String,
            transactionDate: String,
            walletId: Int,
            categoryId: Int
        ): TransactionCreateRequest {
            val amountString = when (amount) {
                is Double -> amount.toString()
                is Int -> amount.toString()
                is String -> amount
                else -> throw IllegalArgumentException("Unsupported amount type")
            }

            return TransactionCreateRequest(
                amount = amountString,
                note = note,
                type = type,
                transactionDate = transactionDate,
                walletId = walletId,
                categoryId = categoryId
            )
        }
    }
}