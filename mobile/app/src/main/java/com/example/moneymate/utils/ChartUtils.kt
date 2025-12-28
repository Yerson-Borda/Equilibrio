package com.example.moneymate.utils

import com.example.domain.transaction.model.DailyData
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

object ChartUtils {

    fun smartGroupDaysForChart(dailyData: List<DailyData>): List<DailyData> {
        return when {
            dailyData.isEmpty() -> dailyData
            dailyData.size <= 30 -> {
                // For up to 30 days, show all days but format labels properly
                dailyData.map { data ->
                    val originalLabel = data.dayLabel
                    // If label has month name, keep only day number for cleaner display
                    val cleanedLabel = if (originalLabel.contains(" ")) {
                        originalLabel.split(" ")[0]
                    } else {
                        originalLabel
                    }
                    data.copy(dayLabel = cleanedLabel)
                }
            }
            dailyData.size <= 60 -> {
                // For 31-60 days, show every other day with day number only
                dailyData.filterIndexed { index, _ -> index % 2 == 0 }
                    .map { data ->
                        val dayNum = try {
                            LocalDate.parse(data.date).dayOfMonth.toString()
                        } catch (e: Exception) {
                            data.dayLabel.split(" ")[0]
                        }
                        data.copy(dayLabel = dayNum)
                    }
            }
            dailyData.size <= 90 -> {
                // For 61-90 days, show every 3rd day with day number
                dailyData.filterIndexed { index, _ -> index % 3 == 0 }
                    .map { data ->
                        val dayNum = try {
                            LocalDate.parse(data.date).dayOfMonth.toString()
                        } catch (e: Exception) {
                            data.dayLabel.split(" ")[0]
                        }
                        data.copy(dayLabel = dayNum)
                    }
            }
            else -> {
                // For more than 90 days, group by weeks with week numbers
                dailyData.chunked(7).mapIndexed { weekIndex, week ->
                    val totalIncome = week.sumOf { it.income }
                    val totalExpenses = week.sumOf { it.expenses }
                    val firstDate = try {
                        LocalDate.parse(week.first().date)
                    } catch (e: Exception) {
                        null
                    }

                    val weekLabel = if (firstDate != null) {
                        val weekNum = firstDate.get(java.time.temporal.WeekFields.ISO.weekOfYear())
                        val monthAbbr = firstDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        "W$weekNum\n$monthAbbr"
                    } else {
                        "Week ${weekIndex + 1}"
                    }

                    DailyData(
                        date = week.first().date,
                        dayLabel = weekLabel,
                        income = totalIncome,
                        expenses = totalExpenses
                    )
                }
            }
        }
    }

    // New function to ensure we don't cut off chart data
    fun prepareChartLabels(days: List<DailyData>): List<String> {
        return when {
            days.isEmpty() -> emptyList()
            days.size <= 15 -> {
                // For small datasets, show every label
                days.map { it.dayLabel }
            }
            days.size <= 30 -> {
                // Show every other label
                days.mapIndexed { index, data ->
                    if (index % 2 == 0) data.dayLabel else ""
                }
            }
            else -> {
                // Show only selected labels to prevent overlap
                val step = when (days.size) {
                    in 31..45 -> 3
                    in 46..60 -> 4
                    in 61..90 -> 5
                    else -> 6
                }

                days.mapIndexed { index, data ->
                    if (index % step == 0) {
                        // For long ranges, show month abbreviations occasionally
                        if (step >= 5 && index % (step * 2) == 0) {
                            try {
                                val date = LocalDate.parse(data.date)
                                date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            } catch (e: Exception) {
                                data.dayLabel
                            }
                        } else {
                            data.dayLabel
                        }
                    } else {
                        ""
                    }
                }
            }
        }
    }

    // Helper to get short month name
    private fun getShortMonthName(date: LocalDate): String {
        return date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
}