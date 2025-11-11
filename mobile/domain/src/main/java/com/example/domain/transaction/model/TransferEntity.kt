package com.example.domain.transaction.model

data class TransferEntity(
    val message: String,
    val sourceTransaction: TransactionEntity,
    val destinationTransaction: TransactionEntity,
    val exchangeRate: Double,
    val convertedAmount: Double
)