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
import com.example.domain.user.model.StatsData

@Composable
fun RegularHomeContent(
    stats: StatsData,
    onSeeAllBudget: () -> Unit,
    onSeeAllTransactions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Show basic stats since we don't have detailed data yet
        BasicStatsSection(stats = stats)
        Spacer(modifier = Modifier.height(24.dp))

        // Placeholder for future features
        ComingSoonSection()
    }
}

@Composable
fun BasicStatsSection(stats: StatsData) {
    Column {
        Text(
            text = "Overview",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

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
                Text(
                    text = "Financial Summary",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Wallets:",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${stats.walletCount}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total Transactions:",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${stats.totalTransactions}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ComingSoonSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "More features coming soon!",
            color = Color(0xFFAAAAAA),
            fontSize = 14.sp
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