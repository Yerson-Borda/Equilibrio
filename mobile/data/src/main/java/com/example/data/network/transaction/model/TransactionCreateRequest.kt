package com.example.data.network.transaction.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionCreateRequest(
    @SerialName("amount") val amount: String, // Always send as string to avoid issues
    @SerialName("description") val description: String? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("type") val type: String,
    @SerialName("transaction_date") val transactionDate: String,
    @SerialName("wallet_id") val walletId: Int,
    @SerialName("category_id") val categoryId: Int
) {
    companion object {
        fun create(
            amount: Any, // Accept Any but convert to String
            description: String? = null,
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
                description = description,
                note = note,
                type = type,
                transactionDate = transactionDate,
                walletId = walletId,
                categoryId = categoryId
            )
        }
    }
}