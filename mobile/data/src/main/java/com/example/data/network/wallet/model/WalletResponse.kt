// data/network/wallet/model/WalletResponse.kt
package com.example.data.network.wallet.model

import com.example.domain.wallet.model.Wallet
import kotlinx.serialization.Serializable

@Serializable
data class WalletResponse(
    val id: Int,
    val name: String,
    val currency: String,
    val wallet_type: String,
    val card_number: String? = null,
    val color: String,
    val balance: String, // Changed from nullable to non-nullable
    val user_id: Int,
    val created_at: String
) {
    fun toDomain(): Wallet {
        return Wallet(
            id = id,
            name = name,
            currency = currency,
            walletType = wallet_type,
            initialBalance = balance, // Map balance to initialBalance
            cardNumber = card_number,
            color = color,
            balance = balance,
            userId = user_id,
            createdAt = created_at
        )
    }
}