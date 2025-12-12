// data/network/wallet/model/WalletCreateRequest.kt
package com.example.data.network.wallet.model

import kotlinx.serialization.Serializable
import com.example.domain.wallet.model.WalletCreateRequest as DomainWalletCreateRequest

@Serializable
data class WalletCreateRequest(
    val name: String,
    val currency: String,
    val wallet_type: String,
    val card_number: String? = null,
    val color: String,
    val balance: Double // Changed from initial_balance to balance
) {
    companion object {
        fun fromDomain(domain: DomainWalletCreateRequest): WalletCreateRequest {
            return WalletCreateRequest(
                name = domain.name,
                currency = domain.currency,
                wallet_type = domain.walletType,
                card_number = domain.cardNumber,
                color = domain.color,
                balance = domain.initialBalance // Map initialBalance to balance
            )
        }
    }
}