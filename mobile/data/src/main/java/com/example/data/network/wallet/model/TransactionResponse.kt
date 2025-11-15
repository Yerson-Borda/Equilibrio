// data/network/wallet/model/TransactionResponse.kt
package com.example.data.network.wallet.model

import kotlinx.serialization.Serializable
import com.example.domain.wallet.model.Transaction as DomainTransaction

@Serializable
data class TransactionResponse(
    val id: Int,
    val amount: String,
    val description: String? = null,
    val note: String? = null,
    val type: String,
    val transaction_date: String,
    val wallet_id: Int,
    val category_id: Int,
    val user_id: Int,
    val created_at: String
) {
    fun toDomain(): DomainTransaction {
        return DomainTransaction(
            id = id,
            amount = amount,
            description = description,
            note = note,
            type = type,
            date = transaction_date,
            walletId = wallet_id,
            categoryId = category_id,
            userId = user_id,
            createdAt = created_at
        )
    }
}