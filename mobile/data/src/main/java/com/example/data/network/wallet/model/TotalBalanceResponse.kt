package com.example.data.network.wallet.model

import com.example.domain.wallet.model.TotalBalance
import kotlinx.serialization.Serializable

@Serializable
data class TotalBalanceResponse(
    val total_balance: Double,
    val currency: String,
    val breakdown: List<BalanceBreakdown>
)

@Serializable
data class BalanceBreakdown(
    val wallet_id: Int,
    val wallet_name: String,
    val wallet_type: String,
    val original_balance: Double,
    val original_currency: String,
    val converted_balance: Double,
    val converted_currency: String,
    val exchange_rate_used: Double
)

fun TotalBalanceResponse.toDomain(): TotalBalance {
    return TotalBalance(
        totalBalance = total_balance,
        currency = currency,
        breakdown = breakdown.map { it.toDomain() }
    )
}

fun BalanceBreakdown.toDomain(): com.example.domain.wallet.model.BalanceBreakdown {
    return com.example.domain.wallet.model.BalanceBreakdown(
        walletId = wallet_id,
        walletName = wallet_name,
        walletType = wallet_type,
        originalBalance = original_balance,
        originalCurrency = original_currency,
        convertedBalance = converted_balance,
        convertedCurrency = converted_currency,
        exchangeRateUsed = exchange_rate_used
    )
}