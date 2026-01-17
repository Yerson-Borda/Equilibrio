package com.example.moneymate.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.budget.model.Budget
import com.example.domain.transaction.model.TransactionEntity
import com.example.moneymate.R
import com.example.moneymate.ui.components.TransactionsSection
import kotlin.math.cos
import kotlin.math.sin

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
            .padding(horizontal = 20.dp)
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
                shape = RoundedCornerShape(10.dp)
            )
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
                progress = (budget.monthlySpent / budget.monthlyLimit).toFloat(),
                limit = budget.monthlyLimit,
                pointerIconResId = painterResource(R.drawable.pointer_ic)
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

                Row {
                    Text(
                        text = "$currencySymbol${"%.0f".format(budget.monthlySpent)}/",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$currencySymbol${"%.0f".format(budget.monthlyLimit)}",
                        color = Color(0xFF4D6BFA),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                progress = (budget.monthlySpent / budget.monthlyLimit).toFloat(),
                limit = budget.monthlyLimit,
                pointerIconResId = painterResource(R.drawable.pointer_ic)
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
                shape = RoundedCornerShape(10.dp)
            )
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
    limit: Double,
    pointerIconResId: Painter
) {
    val hasLimit = limit > 0
    val displayProgress = if (hasLimit) progress.coerceIn(0f, 1f) else 1f
    val pointerAngle = (360f * displayProgress) - 90f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(64.dp)
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCircle(
                color = Color(0xFFF2F2F7),
                radius = size.minDimension / 2
            )
        }
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawArc(
                color = if (hasLimit) Color(0xFF007AFF) else Color(0xFF666666),
                startAngle = -90f,
                sweepAngle = 360f * displayProgress,
                useCenter = false,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                ),
                size = Size(
                    width = size.width - 8.dp.toPx(),
                    height = size.height - 8.dp.toPx()
                )
            )
        }
        Image(
            painter = pointerIconResId,
            contentDescription = "Progress pointer",
            modifier = Modifier
                .width(31.dp)
                .height(24.dp)
                .graphicsLayer {
                    rotationZ = -pointerAngle - 40f
                }
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