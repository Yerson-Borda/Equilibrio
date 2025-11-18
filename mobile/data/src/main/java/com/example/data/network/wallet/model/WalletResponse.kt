// data/network/wallet/model/WalletResponse.kt
package com.example.data.network.wallet.model

import com.example.domain.wallet.model.Wallet
import kotlinx.serialization.Serializable

@Serializable
data class WalletResponse(
    val id: Int? = null,
    val name: String,
    val currency: String = "USD",
    val wallet_type: String,
    val balance: String, // This is the current balance
    val card_number: String? = null,
    val color: String = "#3B82F6",
    val user_id: Int? = null,
    val created_at: String? = null
) {
    fun toDomain(): Wallet {
        return Wallet(
            id = id ?: throw IllegalArgumentException("Wallet ID cannot be null"),
            name = name,
            currency = currency,
            walletType = wallet_type,
            initialBalance = balance, // Use balance as initial balance
            cardNumber = card_number,
            color = color,
            balance = balance, // Also set current balance
            userId = user_id,
            createdAt = created_at
        )
    }
}