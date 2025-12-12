package com.example.data.network.transaction.model

import com.example.domain.transaction.model.CreateTransaction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionCreateRequest(
    @SerialName("name") val name: String,
    @SerialName("amount") val amount: String, // Serialized as string, but we accept Any
    @SerialName("note") val note: String? = null,
    @SerialName("type") val type: String,
    @SerialName("transaction_date") val transactionDate: String,
    @SerialName("wallet_id") val walletId: Int,
    @SerialName("category_id") val categoryId: Int,
    @SerialName("tags") val tags: List<Int> = emptyList()
) {
    companion object {
        fun fromDomain(createTransaction:CreateTransaction): TransactionCreateRequest {
            val amountString = when (val amount = createTransaction.amount) {
                is Double -> amount.toString()
                is Float -> amount.toString()
                is Int -> amount.toString()
                is Long -> amount.toString()
                is String -> amount
                else -> throw IllegalArgumentException("Unsupported amount type: ${amount::class.simpleName}")
            }

            return TransactionCreateRequest(
                name = createTransaction.name,
                amount = amountString,
                note = createTransaction.note,
                type = createTransaction.type,
                transactionDate = createTransaction.transactionDate,
                walletId = createTransaction.walletId,
                categoryId = createTransaction.categoryId,
                tags = createTransaction.tags
            )
        }
    }
}