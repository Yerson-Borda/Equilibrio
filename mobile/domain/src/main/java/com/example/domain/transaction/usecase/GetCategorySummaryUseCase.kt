package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.CategorySummaryData
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class GetCategorySummaryUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend fun execute(
        startDate: String = getDefaultStartDate(),
        endDate: String = getDefaultEndDate()
    ): CategorySummaryData {
        return try {
            transactionRepository.getCategorySummary(startDate, endDate).getOrThrow()
        } catch (e: Exception) {
            CategorySummaryData(
                expenses = emptyList(),
                incomes = emptyList(),
                totalExpenses = 0.0,
                totalIncomes = 0.0,
                netFlow = 0.0
            )
        }
    }

    private fun getDefaultStartDate(): String {
        // Using Calendar instead of LocalDate
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30) // 30 days ago
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun getDefaultEndDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}