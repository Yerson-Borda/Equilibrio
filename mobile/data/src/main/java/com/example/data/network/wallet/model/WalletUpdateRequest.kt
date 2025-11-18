// data/network/wallet/model/WalletUpdateRequest.kt
package com.example.data.network.wallet.model

import com.example.domain.wallet.model.WalletUpdateRequest as DomainWalletUpdateRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletUpdateRequest(
    @SerialName("name") val name: String? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("wallet_type") val walletType: String? = null,
    @SerialName("balance") val balance: String? = null, // Changed to 'balance' and String
    @SerialName("card_number") val cardNumber: String? = null,
    @SerialName("color") val color: String? = null
) {
    companion object {
        fun fromDomain(domain: DomainWalletUpdateRequest): WalletUpdateRequest {
            return WalletUpdateRequest(
                name = domain.name,
                currency = domain.currency,
                walletType = domain.walletType,
                balance = domain.initialBalance, // Use the string directly
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