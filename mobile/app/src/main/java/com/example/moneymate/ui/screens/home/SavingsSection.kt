package com.example.moneymate.ui.screens.home

import androidx.compose.foundation.background
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

@Composable
fun SavingsSection() {
    Column {
        Text(
            text = "Savings",
            color = Color(0xFF1E1E1E),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        val savingsItems = listOf(
            SavingsItem("Phone 13 Mini", 699.0),
            SavingsItem("Car", 20000.0),
            SavingsItem("Facebook Pro M1", 1499.0),
            SavingsItem("House", 30500.0)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            savingsItems.forEach { item ->
                SavingsItemRow(item)
            }
        }
    }
}

@Composable
fun SavingsItemRow(item: SavingsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.name,
            color = Color.White,
            fontSize = 16.sp
        )

        Text(
            text = "$${item.targetAmount.toInt()}",
            color = Color(0xFF2196F3),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}