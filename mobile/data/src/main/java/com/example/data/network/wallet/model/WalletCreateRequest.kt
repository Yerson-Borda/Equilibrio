// data/network/wallet/model/WalletCreateRequest.kt
package com.example.data.network.wallet.model

import kotlinx.serialization.Serializable
import com.example.domain.wallet.model.WalletCreateRequest as DomainWalletCreateRequest

@Serializable
data class WalletCreateRequest(
    val name: String,
    val currency: String = "USD",
    val wallet_type: String,
    val balance: String, // Change from initial_balance to balance and make it String
    val card_number: String? = null,
    val color: String = "#3B82F6"
) {
    companion object {
        fun fromDomain(domain: DomainWalletCreateRequest): WalletCreateRequest {
            return WalletCreateRequest(
                name = domain.name,
                currency = domain.currency,
                wallet_type = domain.walletType,
                balance = domain.initialBalance.toString(), // Convert to String
                card_number = domain.cardNumber,
                color = domain.color
            )
        }
    }
}