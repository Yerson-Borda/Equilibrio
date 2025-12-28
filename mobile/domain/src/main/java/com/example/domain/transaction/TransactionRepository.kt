package com.example.domain.transaction


import com.example.domain.transaction.model.AverageSpendingData
import com.example.domain.transaction.model.CategorySummaryData
import com.example.domain.transaction.model.ComparisonCategoryData
import com.example.domain.transaction.model.CreateTransaction
import com.example.domain.transaction.model.DailyData
import com.example.domain.transaction.model.PeriodFilter
import com.example.domain.transaction.model.SpendingTrendData
import com.example.domain.transaction.model.TopCategoryData
import com.example.domain.transaction.model.TransactionEntity
import com.example.domain.transaction.model.TransferEntity

interface TransactionRepository {
    suspend fun createTransaction(
        createTransaction: CreateTransaction
    ): Result<TransactionEntity>

    suspend fun createTransfer(
        sourceWalletId: Int,
        destinationWalletId: Int,
        amount: Any,
        note: String?
    ): Result<TransferEntity>

    suspend fun getTransactions(): Result<List<TransactionEntity>>

    suspend fun deleteTransaction(id: Int): Result<Unit>

    suspend fun getTransactionsByWalletId(walletId: Int): Result<List<TransactionEntity>>

    suspend fun getSpendingTrends(months: Int): Result<List<SpendingTrendData>>

    // ADD NEW METHODS
    suspend fun getCategorySummary(
        startDate: String,  // Format: "2024-03-20"
        endDate: String     // Format: "2024-04-20"
    ): Result<CategorySummaryData>

    suspend fun getMonthlyComparison(
        month: String       // Format: "2024-03"
    ): Result<List<ComparisonCategoryData>>

    suspend fun getDailySpendingData(
        startDate: String,  // Format: "2024-03-20"
        endDate: String     // Format: "2024-04-20"
    ): Result<List<DailyData>>

    suspend fun getTransactionsByDateRange(
        startDate: String,
        endDate: String
    ): Result<List<TransactionEntity>>

    suspend fun getTopCategoriesCurrentMonth(): Result<List<TopCategoryData>>

    suspend fun getAverageSpending(period: PeriodFilter): Result<List<AverageSpendingData>>
}