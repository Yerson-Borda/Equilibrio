package com.example.moneymate.ui.screens.transaction.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.transaction.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction. name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = formatTransactionDate(transaction.transactionDate),
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }

        Text(
            text = formatAmount(transaction.amount.toString(), transaction.type),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = getAmountColor(transaction.type)
        )
    }
}

private fun formatTransactionDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString
    }
}

private fun formatAmount(amount: String, type: String): String {
    val sign = when (type) {
        "income" -> "+"
        "expense" -> "-"
        else -> ""
    }
    return "$sign$$amount"
}

private fun getAmountColor(type: String): Color {
    return when (type) {
        "income" -> Color(0xFF4ECDC4)
        "expense" -> Color(0xFFFF6B6B)
        else -> Color(0xFF666666)
    }
}