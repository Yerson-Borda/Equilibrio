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
import kotlin.math.abs

@Composable
fun TransactionsSection(onSeeAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transactions",
                color = Color(0xFF1E1E1E),
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

        Spacer(modifier = Modifier.height(12.dp))

        val transactions = listOf(
            Transaction("Adobe Illustrator", "Subscriptions", -32.0, "Today"),
            Transaction("Dribbble", "Subscriptions", -15.0, "Today"),
            Transaction("Sony Camera", "Shopping", -200.0, "Today"),
            Transaction("Paypal", "Income", 32.0, "Today")
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            transactions.forEach { transaction ->
                TransactionItemRow(transaction)
            }
        }
    }
}

@Composable
fun TransactionItemRow(transaction: Transaction) {
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF333333), CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.merchant,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = transaction.category,
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp
            )
        }

        Text(
            text = if (transaction.amount >= 0) "+$${transaction.amount}" else "-$${abs(transaction.amount)}",
            color = if (transaction.amount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}