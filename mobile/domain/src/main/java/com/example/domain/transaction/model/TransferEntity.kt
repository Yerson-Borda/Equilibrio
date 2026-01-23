package com.example.domain.transaction.model

data class TransferEntity(
    val message: String,
    val sourceTransaction: TransactionEntity,
    val destinationTransaction: TransactionEntity,
    val exchangeRate: Double,
    val convertedAmount: Double
)

data class TransferPreview(
    val sourceCurrency: String,
    val destinationCurrency: String,
    val amount: Double,
    val exchangeRate: Double,
    val convertedAmount: Double
)