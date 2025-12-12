package com.example.data.network.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class WalletBalanceResponse(
    val balance: String
)