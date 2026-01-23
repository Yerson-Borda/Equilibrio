package com.example.data.network.transaction.model

import com.example.domain.transaction.model.TransferEntity
import com.example.domain.transaction.model.TransferPreview
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferDto(
    @SerialName("message") val message: String,
    @SerialName("source_transaction") val sourceTransaction: TransactionDto,
    @SerialName("destination_transaction") val destinationTransaction: TransactionDto,
    @SerialName("exchange_rate") val exchangeRate: Double,
    @SerialName("converted_amount") val convertedAmount: Double
) {
    fun toEntity(): TransferEntity {
        return TransferEntity(
            message = message,
            sourceTransaction = sourceTransaction.toEntity(),
            destinationTransaction = destinationTransaction.toEntity(),
            exchangeRate = exchangeRate,
            convertedAmount = convertedAmount
        )
    }
}

@Serializable
data class TransferPreviewResponse(
    @SerialName("source_currency") val sourceCurrency: String,
    @SerialName("destination_currency") val destinationCurrency: String,
    @SerialName("amount") val amount: String,
    @SerialName("exchange_rate") val exchangeRate: String,
    @SerialName("converted_amount") val convertedAmount: String
) {
    fun toDomain(): TransferPreview {
        return TransferPreview(
            sourceCurrency = sourceCurrency,
            destinationCurrency = destinationCurrency,
            amount = amount.toDoubleOrNull() ?: 0.0,
            exchangeRate = exchangeRate.toDoubleOrNull() ?: 1.0,
            convertedAmount = convertedAmount.toDoubleOrNull() ?: 0.0
        )
    }
}