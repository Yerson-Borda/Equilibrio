package com.example.moneymate.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.home.model.Transaction
import kotlin.math.abs

@Composable
fun TransactionsSection(
    transactions: List<Transaction>,
    onSeeAllTransactions: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                color = Color(0xFF1E1E1E),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "See All",
                color = Color(0xFF2196F3),
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSeeAllTransactions() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
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
                    text = "No recent transactions",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                transactions.take(5).forEach { transaction -> // Show only last 5 transactions
                    TransactionItemRow(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(transaction: Transaction) {
    val isIncome = transaction.type == "income"
    val amountColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    val amountPrefix = if (isIncome) "+" else "-"
    val iconBackground = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = transaction.category.first().toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.merchant,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${transaction.category} • ${formatTransactionDate(transaction.date)}",
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp
            )
        }

        Text(
            text = "$amountPrefix$${"%.2f".format(abs(transaction.amount))}",
            color = amountColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Helper function to format transaction date
private fun formatTransactionDate(dateString: String): String {
    return try {
        // You might want to use a proper date formatter based on your backend date format
        // For now, just return the date as is or do simple formatting
        if (dateString.contains("T")) {
            dateString.substring(0, 10) // Extract just the date part from ISO format
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}