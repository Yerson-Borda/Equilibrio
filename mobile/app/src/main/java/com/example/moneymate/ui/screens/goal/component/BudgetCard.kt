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
fun BudgetCard(
    title: String,
    limit: String,
    spent: String,
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1E1E1E),
    textColor: Color = Color.White
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
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            BudgetAmountRow(
                limitLabel = "Budget",
                limitValue = limit,
                spentLabel = "Spent",
                spentValue = spent
            )

            Spacer(modifier = Modifier.height(12.dp))

            BudgetProgressBar(
                progress = progress,
                progressColor = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun DailyLimitCard(
    title: String,
    limit: String,
    spent: String,
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1E1E1E),
    textColor: Color = Color.White
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
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Limit and Spent row
            BudgetAmountRow(
                limitLabel = "Limit",
                limitValue = limit,
                spentLabel = "Spent",
                spentValue = spent
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            BudgetProgressBar(
                progress = progress,
                progressColor = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
fun BudgetAmountRow(
    limitLabel: String,
    limitValue: String,
    spentLabel: String,
    spentValue: String,
    textColor: Color = Color.White
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = limitLabel,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.6f)
            )
            Text(
                text = limitValue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = androidx.compose.ui.Alignment.End
        ) {
            Text(
                text = spentLabel,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
            Text(
                text = spentValue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}