package com.example.moneymate.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.budget.model.Budget
import com.example.domain.transaction.model.TransactionEntity
import com.example.domain.user.model.StatsData
import com.example.moneymate.ui.components.TransactionsSection

@Composable
fun RegularHomeContent(
    stats: StatsData,
    recentTransactions: List<TransactionEntity>? = null,
    budgetData: Budget? = null,
    currencySymbol: String = "$",
    onSeeAllBudget: () -> Unit,
    onSeeAllTransactions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Budget vs Expense Section
        BudgetExpenseSection(
            budget = budgetData,
            currencySymbol = currencySymbol,
            onSeeAll = onSeeAllBudget
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Savings Goals Section
        SavingsGoalsSection(
            currencySymbol = currencySymbol,
            onSeeAllSavings = onSeeAllBudget // You can create a separate callback if needed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Transactions Section
        TransactionsSection(
            transactions = recentTransactions ?: emptyList(),
            currencySymbol = currencySymbol,
            modifier = Modifier.fillMaxWidth(),
            onSeeAll = onSeeAllTransactions
        )
    }
}

@Composable
fun SavingsGoalsSection(
    currencySymbol: String,
    onSeeAllSavings: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()
        .background(
            color = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    ){
        Column {
            // Header with See All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Savings",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "See All",
                    color = Color(0xFF2196F3),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSeeAllSavings() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Savings goals grid - 2 columns
            Column {
                // First row - 2 cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // iPhone 13 Mini card
                    SavingsGoalCard(
                        goalName = "iPhone 13 Mini",
                        savedAmount = 699.0,
                        targetAmount = 1499.0,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )

                    // Car card
                    SavingsGoalCard(
                        goalName = "Car",
                        savedAmount = 20000.0,
                        targetAmount = 30500.0,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Second row - 2 cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Macbook Pro M1 card
                    SavingsGoalCard(
                        goalName = "Macbook Pro M1",
                        savedAmount = 1200.0,
                        targetAmount = 1499.0,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )

                    // House card
                    SavingsGoalCard(
                        goalName = "House",
                        savedAmount = 15000.0,
                        targetAmount = 30500.0,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SavingsGoalCard(
    goalName: String,
    savedAmount: Double,
    targetAmount: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val progress = (savedAmount / targetAmount).coerceIn(0.0, 1.0)

    Box(
        modifier = modifier
            .background(
                color = Color(0xFFF8F8F8),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Goal name
            Text(
                text = goalName,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Saved amount
            Text(
                text = "$currencySymbol${"%.0f".format(savedAmount)}",
                color = Color(0xFF2196F3),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(3.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.toFloat())
                        .height(6.dp)
                        .background(
                            color = Color(0xFF4D6BFA), // Green for savings
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Target amount
            Text(
                text = "Target: $currencySymbol${"%.0f".format(targetAmount)}",
                color = Color(0xFF666666),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )

            // Progress percentage
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color(0xFF666666),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// The rest of your existing code remains the same...
@Composable
fun BudgetExpenseSection(
    budget: Budget?,
    currencySymbol: String,
    onSeeAll: () -> Unit
) {
    Column {
        // Header with See All
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budget vs Expense",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "See All",
                color = Color(0xFF2196F3),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main content box - changed to white background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White, // Changed to white
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            if (budget != null) {
                ActualBudgetContent(budget, currencySymbol, onSeeAll)
            } else {
                // Show exact design from image with hardcoded values
                DesignBudgetContent(currencySymbol, onSeeAll)
            }
        }
    }
}

@Composable
fun BudgetProgressRow(
    spent: Double,
    total: Double,
    currencySymbol: String
) {
    val progress = (spent / total).coerceIn(0.0, 1.0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Small progress bar on the left
        Box(
            modifier = Modifier
                .width(40.dp) // Small fixed width
                .height(4.dp)
                .background(
                    color = Color(0xFFE0E0E0), // Light gray background
                    shape = RoundedCornerShape(2.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.toFloat())
                    .height(4.dp)
                    .background(
                        color = Color(0xFF2196F3), // Blue progress
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }

        // Progress text on the right
        Text(
            text = "$currencySymbol${"%.0f".format(spent)}/$currencySymbol${"%.0f".format(total)}",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ActualBudgetContent(
    budget: Budget,
    currencySymbol: String,
    onSeeAll: () -> Unit
) {
    // Debug logging
    println("DEBUG: monthlyProgress = ${budget.monthlyProgress}")
    println("DEBUG: monthlySpent = ${budget.monthlySpent}, monthlyLimit = ${budget.monthlyLimit}")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8F8F8),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Check if budget limit is set (non-zero)
            val hasBudgetLimit = budget.monthlyLimit > 0

            if (hasBudgetLimit) {
                // Show normal budget content when limit is set
                BudgetWithLimitContent(budget, currencySymbol)
            } else {
                // Show no budget set content
                NoBudgetLimitContent(budget, currencySymbol)
            }
        }
    }
}

@Composable
fun BudgetWithLimitContent(
    budget: Budget,
    currencySymbol: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Use the computed properties from your Budget model
        val budgetRemaining = budget.monthlyRemaining
        val isOverBudget = budget.isMonthlyExceeded

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = if (isOverBudget) {
                    "You've exceeded your budget by $currencySymbol${"%.2f".format(-budgetRemaining)}"
                } else {
                    "You can spend $currencySymbol${"%.2f".format(budgetRemaining)} more this month"
                },
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Status indicator based on budget usage
            val statusText = when {
                budget.monthlyProgress < 0.7 -> "Normal >"
                budget.monthlyProgress < 0.9 -> "Warning >"
                else -> "Critical >"
            }

            val statusColor = when {
                budget.monthlyProgress < 0.7 -> Color(0xFF4CAF50) // Green
                budget.monthlyProgress < 0.9 -> Color(0xFFFF9800) // Orange
                else -> Color(0xFFF44336) // Red
            }

            Text(
                text = statusText,
                color = statusColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Budget usage row with circular progress and text column
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress indicator
            CircularBudgetProgress(
                progress = budget.monthlyProgress,
                spent = budget.monthlySpent,
                total = budget.monthlyLimit,
                currencySymbol = currencySymbol
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Month's Budget Usage text
                Text(
                    text = "${getMonthName(budget.month)}'s Budget Usage",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Budget amount text
                Text(
                    text = "$currencySymbol${"%.0f".format(budget.monthlySpent)}/$currencySymbol${"%.0f".format(budget.monthlyLimit)}",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun NoBudgetLimitContent(
    budget: Budget,
    currencySymbol: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Show spending without budget limit
        Text(
            text = "You've spent $currencySymbol${"%.2f".format(budget.monthlySpent)} this month",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status - always show "No Budget Set" when limit is 0
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "No Budget Set >",
                color = Color(0xFF666666), // Gray for no budget
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Budget usage row with circular progress showing 100% (full circle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress indicator - show full circle when no limit
            CircularBudgetProgress(
                progress = 1f, // Full circle
                spent = budget.monthlySpent,
                total = 0.0, // No limit
                currencySymbol = currencySymbol
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Month's Budget Usage text
                Text(
                    text = "${getMonthName(budget.month)}'s Spending",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Spending amount text (no limit)
                Text(
                    text = "$currencySymbol${"%.0f".format(budget.monthlySpent)} spent",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Also update the DesignBudgetContent to match
@Composable
fun DesignBudgetContent(
    currencySymbol: String,
    onSeeAll: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8F8F8),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        // Since your actual data shows no budget limit, let's show that scenario
        NoBudgetLimitContent(
            budget = Budget(
                id = 1,
                month = 11,
                year = 2025,
                monthlyLimit = 0.0,
                dailyLimit = 0.0,
                monthlySpent = 276.0,
                dailySpent = 25.0,
                lastUpdatedDate = "2025-11-28",
                createdAt = "2025-11-19T21:33:15"
            ),
            currencySymbol = currencySymbol
        )
    }
}

@Composable
fun CircularBudgetProgress(
    progress: Float,
    spent: Double,
    total: Double,
    currencySymbol: String
) {
    // Handle zero total (no budget limit)
    val hasLimit = total > 0
    val displayProgress = if (hasLimit) progress.coerceIn(0f, 1f) else 1f
    val displayText = if (hasLimit) "${(displayProgress * 100).toInt()}%" else "∞"

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(48.dp)
    ) {
        // Background circle
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(48.dp)
        ) {
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = size.minDimension / 2 - 4
            )

            // Progress arc
            drawArc(
                color = if (hasLimit) Color(0xFF4D6BFA) else Color(0xFF666666), // Blue for budget, Gray for no limit
                startAngle = -90f,
                sweepAngle = 360f * displayProgress,
                useCenter = false,
                style = Stroke(width = 4f, cap = StrokeCap.Round),
                size = androidx.compose.ui.geometry.Size(
                    width = size.width - 8,
                    height = size.height - 8
                )
            )
        }

        // Progress text in center
        Text(
            text = displayText,
            color = Color.Black,
            fontSize = if (hasLimit) 10.sp else 8.sp, // Smaller font for infinity symbol
            fontWeight = FontWeight.Bold
        )
    }
}

// Helper function to get month name
private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> "This Month"
    }
}

@Composable
fun NoBudgetContent() {
    Column {
        Text(
            text = "No budget set for this month",
            color = Color.Black, // Changed to black for white background
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set a budget to track your spending",
            color = Color(0xFF666666), // Darker gray for white background
            fontSize = 14.sp
        )
    }
}