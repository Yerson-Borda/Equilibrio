package com.example.data.network.wallet.model

import com.example.domain.wallet.model.WalletUpdateRequest as DomainWalletUpdateRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletUpdateRequest(
    @SerialName("name") val name: String,
    @SerialName("currency") val currency: String = "USD",
    @SerialName("wallet_type") val walletType: String,
    @SerialName("initial_balance") val initialBalance: Double = 0.0,
    @SerialName("card_number") val cardNumber: String? = null,
    @SerialName("color") val color: String = "#3B82F6"
) {
    companion object {
        fun fromDomain(domain: DomainWalletUpdateRequest): WalletUpdateRequest {
            return WalletUpdateRequest(
                name = domain.name,
                currency = domain.currency,
                walletType = domain.walletType,
                initialBalance = domain.initialBalance,
                cardNumber = domain.cardNumber,
                color = domain.color
            )
        }
    }
}

// Extension function to convert domain to data
fun DomainWalletUpdateRequest.toData(): WalletUpdateRequest {
    return WalletUpdateRequest.fromDomain(this)
}