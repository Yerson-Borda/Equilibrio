package com.example.domain.transaction.usecase

import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.MonthlyComparisonData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GetMonthlyComparisonUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend fun execute(month: String = getCurrentMonth()): MonthlyComparisonData {
        return try {
            val categories = transactionRepository.getMonthlyComparison(month).getOrThrow()
            MonthlyComparisonData(
                categories = categories,
                selectedMonth = formatMonthDisplay(month)
            )
        } catch (e: Exception) {
            MonthlyComparisonData(
                categories = emptyList(),
                selectedMonth = formatMonthDisplay(month)
            )
        }
    }

    private fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun formatMonthDisplay(month: String): String {
        // Simple parsing - assumes format "yyyy-MM"
        val parts = month.split("-")
        return if (parts.size == 2) {
            val year = parts[0]
            val monthNum = parts[1].toIntOrNull() ?: 1
            val monthNames = arrayOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            val monthName = monthNames.getOrNull(monthNum - 1) ?: "Unknown"
            "$monthName $year"
        } else {
            "Current Month"
        }
    }
}