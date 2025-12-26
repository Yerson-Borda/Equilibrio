package com.example.data.network.transaction.model

import com.example.domain.transaction.model.TransferEntity
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