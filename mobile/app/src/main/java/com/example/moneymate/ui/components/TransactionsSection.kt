package com.example.moneymate.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.transaction.model.TransactionEntity

@Composable
private fun TransactionItem(
    transaction: TransactionEntity,
    currencySymbol: String = "$"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.note?.ifEmpty { "Transaction" } ?: "Transaction",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Category ID: ${transaction.categoryId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }

            Text(
                text = if (transaction.type == "income")
                    "+$currencySymbol${transaction.amount}"
                else
                    "-$currencySymbol${transaction.amount}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (transaction.type == "income") Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TransactionsSection(
    transactions: List<TransactionEntity>,
    currencySymbol: String = "$",
    onSeeAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
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

        if (transactions.isEmpty()) {
            EmptyTransactionsState()
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currencySymbol = currencySymbol
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactionsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )
    }
}