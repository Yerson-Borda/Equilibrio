// data/network/wallet/model/WalletCreateRequest.kt
package com.example.data.network.wallet.model

import kotlinx.serialization.Serializable
import com.example.domain.wallet.model.WalletCreateRequest as DomainWalletCreateRequest

@Serializable
data class WalletCreateRequest(
    val name: String,
    val currency: String = "USD",
    val wallet_type: String,
    val initial_balance: Double = 0.0,
    val card_number: String? = null,
    val color: String = "#3B82F6"
) {
    companion object {
        fun fromDomain(domain: DomainWalletCreateRequest): WalletCreateRequest {
            return WalletCreateRequest(
                name = domain.name,
                currency = domain.currency,
                wallet_type = domain.walletType,
                initial_balance = domain.initialBalance,
                card_number = domain.cardNumber,
                color = domain.color
            )
        }
    }
}