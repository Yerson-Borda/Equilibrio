package com.example.data.network.transaction.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferCreateRequest(
    @SerialName("source_wallet_id") val sourceWalletId: Int,
    @SerialName("destination_wallet_id") val destinationWalletId: Int,
    @SerialName("amount") val amount: String, // number | string in swagger
    @SerialName("note") val note: String? = null
) {
    companion object {
        fun create(
            sourceWalletId: Int,
            destinationWalletId: Int,
            amount: Any,
            note: String? = null
        ): TransferCreateRequest {
            val amountString = when (amount) {
                is Double -> amount.toString()
                is Int -> amount.toString()
                is String -> amount
                else -> throw IllegalArgumentException("Unsupported amount type: ${amount::class.java}")
            }

            return TransferCreateRequest(
                sourceWalletId = sourceWalletId,
                destinationWalletId = destinationWalletId,
                amount = amountString,
                note = note
            )
        }
    }
}