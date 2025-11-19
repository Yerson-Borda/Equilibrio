package com.example.data.network.transaction.model

import com.example.domain.transaction.model.TransactionEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    @SerialName("id") val id: Int,
    @SerialName("amount") val amount: String, // Matches swagger format
    @SerialName("note") val note: String?, // Note comes before type in swagger
    @SerialName("type") val type: String, // "income", "expense", "transfer"
    @SerialName("transaction_date") val transactionDate: String, // date format
    @SerialName("wallet_id") val walletId: Int,
    @SerialName("category_id") val categoryId: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("created_at") val createdAt: String // date-time format
    // Removed description field as it's not in swagger TransactionResponse
) {
    fun toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            amount = amount,
            description = null, // Description not available in API response
            note = note,
            type = type,
            transactionDate = transactionDate,
            walletId = walletId,
            categoryId = categoryId,
            userId = userId,
            createdAt = createdAt
        )
    }
}