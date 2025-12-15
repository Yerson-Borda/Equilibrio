package com.example.moneymate.ui.screens.goal.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BalanceCard(
    balance: String,
    vsLastMonth: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1E1E1E),
    textColor: Color = Color.White,
    positiveColor: Color = Color(0xFF4CAF50)
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "Available Balance",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$$balance",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(
                text = vsLastMonth,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = positiveColor
            )
        }
    }
}