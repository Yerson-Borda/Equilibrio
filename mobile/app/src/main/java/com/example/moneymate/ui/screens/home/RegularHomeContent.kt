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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.home.model.HomeData

@Composable
fun RegularHomeContent(
    homeData: HomeData,
    onSeeAllBudget: () -> Unit,
    onSeeAllTransactions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        BudgetExpenseSection(
            budgetRemaining = homeData.budgetRemaining,
            budgetStatus = homeData.budgetStatus,
            onSeeAll = onSeeAllBudget
        )
        Spacer(modifier = Modifier.height(24.dp))
        SavingsSection(savingsGoals = homeData.savingsGoals)
        Spacer(modifier = Modifier.height(24.dp))
        TransactionsSection(
            transactions = homeData.recentTransactions,
            onSeeAllTransactions = onSeeAllTransactions
        )
    }
}

@Composable
fun BudgetExpenseSection(
    budgetRemaining: Double?,
    budgetStatus: String?,
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
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "See All",
                color = Color(0xFF2196F3),
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                if (budgetRemaining != null) {
                    Text(
                        text = "You can spend $${"%.2f".format(budgetRemaining)} more this month",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = budgetStatus ?: "No Budget Set",
                        color = when (budgetStatus?.lowercase()) {
                            "normal", "on track" -> Color(0xFF4CAF50)
                            "warning", "close to limit" -> Color(0xFFFF9800)
                            "exceeded", "over budget" -> Color(0xFFF44336)
                            else -> Color(0xFFAAAAAA)
                        },
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "No budget set for this month",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Set a budget to track your spending",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}