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
import com.example.moneymate.ui.components.TransactionsSection

@Composable
fun RegularHomeContent(
    recentTransactions: List<TransactionEntity>? = null,
    budgetData: Budget? = null,
    currencySymbol: String = "$",
    onSeeAllBudget: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    isInLazyColumn: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        BudgetExpenseSection(
            budget = budgetData,
            currencySymbol = currencySymbol,
            onSeeAll = onSeeAllBudget
        )

        Spacer(modifier = Modifier.height(24.dp))

        SavingsGoalsSection(
            currencySymbol = currencySymbol,
            onSeeAllSavings = onSeeAllBudget
        )

        Spacer(modifier = Modifier.height(24.dp))

        TransactionsSection(
            transactions = recentTransactions ?: emptyList(),
            currencySymbol = currencySymbol,
            modifier = Modifier.fillMaxWidth(),
            onSeeAll = onSeeAllTransactions,
            isInLazyColumn = isInLazyColumn
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

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SavingsGoalCard(
                        goalName = "iPhone 13 Mini",
                        savedAmount = 699.0,
                        targetAmount = 1499.0,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )

                    SavingsGoalCard(
                        goalName = "Car",
                        savedAmount = 20000.0,
                        targetAmount = 30500.0,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SavingsGoalCard(
                        goalName = "Macbook Pro M1",
                        savedAmount = 1200.0,
                        targetAmount = 1499.0,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )

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
            Text(
                text = goalName,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$currencySymbol${"%.0f".format(savedAmount)}",
                color = Color(0xFF2196F3),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
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
                            color = Color(0xFF4D6BFA),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Target: $currencySymbol${"%.0f".format(targetAmount)}",
                color = Color(0xFF666666),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color(0xFF666666),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BudgetExpenseSection(
    budget: Budget?,
    currencySymbol: String,
    onSeeAll: () -> Unit
) {
    Column {
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            if (budget != null) {
                ActualBudgetContent(budget, currencySymbol)
            } else {
                DesignBudgetContent(currencySymbol)
            }
        }
    }
}
@Composable
fun ActualBudgetContent(
    budget: Budget,
    currencySymbol: String,
) {
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
            val hasBudgetLimit = budget.monthlyLimit > 0
            if (hasBudgetLimit) {
                BudgetWithLimitContent(budget, currencySymbol)
            } else {
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
            val statusText = when {
                budget.monthlyProgress < 0.7 -> "Normal >"
                budget.monthlyProgress < 0.9 -> "Warning >"
                else -> "Critical >"
            }

            val statusColor = when {
                budget.monthlyProgress < 0.7 -> Color(0xFF4CAF50)
                budget.monthlyProgress < 0.9 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }

            Text(
                text = statusText,
                color = statusColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularBudgetProgress(
                progress = budget.monthlyProgress,
                total = budget.monthlyLimit,
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${getMonthName(budget.month)}'s Budget Usage",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

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
        Text(
            text = "You've spent $currencySymbol${"%.2f".format(budget.monthlySpent)} this month",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "No Budget Set >",
                color = Color(0xFF666666),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularBudgetProgress(
                progress = 1f,
                total = 0.0
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${getMonthName(budget.month)}'s Spending",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
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

@Composable
fun DesignBudgetContent(
    currencySymbol: String
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
    total: Double
) {
    val hasLimit = total > 0
    val displayProgress = if (hasLimit) progress.coerceIn(0f, 1f) else 1f
    val displayText = if (hasLimit) "${(displayProgress * 100).toInt()}%" else "∞"

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(48.dp)
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(48.dp)
        ) {
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = size.minDimension / 2 - 4
            )
            drawArc(
                color = if (hasLimit) Color(0xFF4D6BFA) else Color(0xFF666666),
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
        Text(
            text = displayText,
            color = Color.Black,
            fontSize = if (hasLimit) 10.sp else 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

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