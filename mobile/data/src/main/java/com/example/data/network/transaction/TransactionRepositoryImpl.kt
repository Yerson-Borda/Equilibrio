package com.example.data.network.transaction

import com.example.data.network.transaction.model.AverageSpendingResponse
import com.example.data.network.transaction.model.CategoryResponse
import com.example.data.network.transaction.model.CategorySummaryResponse
import com.example.data.network.transaction.model.MonthlyComparisonResponse
import com.example.data.network.transaction.model.MonthlySummary
import com.example.data.network.transaction.model.TopCategoryResponse
import com.example.data.network.transaction.model.TransactionCreateRequest
import com.example.data.network.transaction.model.TransactionDto
import com.example.data.network.transaction.model.TransferCreateRequest
import com.example.data.network.transaction.model.TransferDto
import com.example.domain.transaction.TransactionRepository
import com.example.domain.transaction.model.AverageSpendingData
import com.example.domain.transaction.model.CategoryData
import com.example.domain.transaction.model.CategorySummaryData
import com.example.domain.transaction.model.ComparisonCategoryData
import com.example.domain.transaction.model.CreateTransaction
import com.example.domain.transaction.model.DailyData
import com.example.domain.transaction.model.PeriodFilter
import com.example.domain.transaction.model.SpendingTrendData
import com.example.domain.transaction.model.TopCategoryData
import com.example.domain.transaction.model.TransactionEntity
import com.example.domain.transaction.model.TransferEntity
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionRepositoryImpl(
    private val apiService: TransactionApi
) : TransactionRepository {

    override suspend fun createTransaction(
        createTransaction: CreateTransaction
    ): Result<TransactionEntity> {
        return try {
            val request = TransactionCreateRequest.fromDomain(createTransaction)

            val response = apiService.createTransaction(request)
            handleTransactionResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTransfer(
        sourceWalletId: Int,
        destinationWalletId: Int,
        amount: Any,
        note: String?
    ): Result<TransferEntity> {
        return try {
            val request = TransferCreateRequest.create(
                sourceWalletId = sourceWalletId,
                destinationWalletId = destinationWalletId,
                amount = amount,
                note = note
            )

            val response = apiService.createTransfer(request)
            handleTransferResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactions(): Result<List<TransactionEntity>> {
        return try {
            val response = apiService.getTransactions()
            if (response.isSuccessful) {
                val transactionDtos = response.body()
                if (transactionDtos != null) {
                    Result.success(transactionDtos.map { it.toEntity() })
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactionsByWalletId(walletId: Int): Result<List<TransactionEntity>> {
        return try {
            val response = apiService.getTransactionsByWalletId(walletId)
            if (response.isSuccessful) {
                val transactionDtos = response.body()
                if (transactionDtos != null) {
                    Result.success(transactionDtos.map { it.toEntity() })
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteTransaction(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSpendingTrends(months: Int): Result<List<SpendingTrendData>> {
        return try {
            val response = apiService.getSpendingTrends(months)
            if (response.isSuccessful) {
                val spendingTrendsResponse = response.body()
                if (spendingTrendsResponse != null) {
                    Result.success(spendingTrendsResponse.monthly_summary.map { it.toDomain() })
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCategorySummary(
        startDate: String,
        endDate: String
    ): Result<CategorySummaryData> {
        return try {
            val response = apiService.getCategorySummary(startDate, endDate)
            if (response.isSuccessful) {
                val body = response.body()
                Result.success(body?.toDomain() ?: getEmptyCategorySummary())
            } else {
                Result.failure(Exception("Failed to fetch category summary"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMonthlyComparison(month: String): Result<List<ComparisonCategoryData>> {
        return try {
            val response = apiService.getMonthlyComparison(month)
            if (response.isSuccessful) {
                val body = response.body()
                Result.success(body?.map { it.toDomain() } ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch monthly comparison"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailySpendingData(startDate: String, endDate: String): Result<List<DailyData>> {
        return try {
            // For now, return sample data
            // In real implementation, you would call an API endpoint
            val sampleData = generateSampleDailyData(startDate, endDate)
            Result.success(sampleData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactionsByDateRange(startDate: String, endDate: String): Result<List<TransactionEntity>> {
        return try {
            val allTransactions = getTransactions().getOrElse { emptyList() }

            // Filter transactions by date range
            val filteredTransactions = allTransactions.filter { transaction ->
                isDateInRange(transaction.transactionDate, startDate, endDate)
            }

            Result.success(filteredTransactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTopCategoriesCurrentMonth(): Result<List<TopCategoryData>> {
        return try {
            val response = apiService.getTopCategoriesCurrentMonth()
            if (response.isSuccessful) {
                val body = response.body()
                Result.success(body?.map { it.toDomain() } ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch top categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAverageSpending(period: PeriodFilter): Result<List<AverageSpendingData>> {
        return try {
            val periodString = when (period) {
                PeriodFilter.DAY -> "day"
                PeriodFilter.MONTH -> "month"
                PeriodFilter.YEAR -> "year"
                PeriodFilter.DAYS_7 -> "day"
                PeriodFilter.DAYS_15 -> "day"
                PeriodFilter.DAYS_30 -> "month"
                PeriodFilter.DAYS_90 -> "month"
            }
            val response = apiService.getAverageSpending(periodString)
            if (response.isSuccessful) {
                val body = response.body()
                Result.success(body?.map { it.toDomain() } ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch average spending"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun TopCategoryResponse.toDomain(): TopCategoryData {
        return TopCategoryData(
            categoryId = category_id,
            categoryName = category_name,
            totalAmount = total_amount
        )
    }

    private fun AverageSpendingResponse.toDomain(): AverageSpendingData {
        return AverageSpendingData(
            categoryId = category_id,
            categoryName = category_name,
            totalPeriodSpent = total_period_spent,
            transactions = transactions,
            periodType = period_type
        )
    }

    private fun isDateInRange(date: String, startDate: String, endDate: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val transactionDate = dateFormat.parse(date)
        val start = dateFormat.parse(startDate)
        val end = dateFormat.parse(endDate)

        return transactionDate in start..end
    }

    private fun generateSampleDailyData(startDate: String, endDate: String): List<DailyData> {
        // Implementation to generate daily data between startDate and endDate
        // This would be replaced with actual API call
        val dailyData = mutableListOf<DailyData>()

        // Parse dates and generate data for each day
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayLabelFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        val start = dateFormat.parse(startDate)
        val end = dateFormat.parse(endDate)

        val calendar = Calendar.getInstance().apply {
            time = start
        }

        while (calendar.time <= end) {
            val date = calendar.time
            val dateString = dateFormat.format(date)
            val dayLabel = dayLabelFormat.format(date)

            // Generate sample data
            val income = 50000.0 + (Math.random() * 30000)
            val expense = 30000.0 + (Math.random() * 20000)

            dailyData.add(
                DailyData(
                    date = dateString,
                    dayLabel = dayLabel,
                    income = income,
                    expenses = expense
                )
            )

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return dailyData
    }

    private fun getEmptyCategorySummary(): CategorySummaryData {
        return CategorySummaryData(
            expenses = emptyList(),
            incomes = emptyList(),
            totalExpenses = 0.0,
            totalIncomes = 0.0,
            netFlow = 0.0
        )
    }

    private fun handleTransactionResponse(response: Response<TransactionDto>): Result<TransactionEntity> {
        return if (response.isSuccessful) {
            val transactionDto = response.body()
            if (transactionDto != null) {
                Result.success(transactionDto.toEntity())
            } else {
                Result.failure(Exception("Failed to create transaction"))
            }
        } else {
            Result.failure(Exception("API error: ${response.code()} - ${response.errorBody()?.string()}"))
        }
    }

    private fun handleTransferResponse(response: Response<TransferDto>): Result<TransferEntity> {
        return if (response.isSuccessful) {
            val transferDto = response.body()
            if (transferDto != null) {
                Result.success(transferDto.toEntity())
            } else {
                Result.failure(Exception("Failed to create transfer"))
            }
        } else {
            Result.failure(Exception("API error: ${response.code()} - ${response.errorBody()?.string()}"))
        }
    }
}

// Extension functions for mapping - USE THESE CONSISTENTLY
private fun MonthlySummary.toDomain(): SpendingTrendData {
    return SpendingTrendData(
        year = year,
        month = month,
        totalSpent = total_spent,
        totalIncome = total_income,
        monthName = month_name,
        displayName = display_name
    )
}

private fun CategorySummaryResponse.toDomain(): CategorySummaryData {
    return CategorySummaryData(
        expenses = expenses.map { it.toDomain() },
        incomes = incomes.map { it.toDomain() },
        totalExpenses = total_expenses,
        totalIncomes = total_incomes,
        netFlow = net_flow
    )
}

private fun CategoryResponse.toDomain(): CategoryData {
    return CategoryData(
        categoryId = category_id,
        categoryName = category_name,
        categoryType = category_type,
        totalAmount = total_amount,
        transactionCount = transaction_count
    )
}

private fun MonthlyComparisonResponse.toDomain(): ComparisonCategoryData {
    return ComparisonCategoryData(
        categoryId = category_id,
        categoryName = category_name,
        currentMonthAmount = current_month_amount,
        previousMonthAmount = previous_month_amount,
        difference = difference,
        percentageChange = percentage_change
    )
}