package com.example.moneymate.ui.screens.goal.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.savingsGoal.model.SavingsGoal
import com.example.moneymate.ui.screens.goal.getMonthName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavingsGoalSection(
    savingsGoal: SavingsGoal?,
    isLoading: Boolean,
    isError: Boolean,
    startDate: Long?, // New parameter
    endDate: Long?,   // New parameter
    onEditClick: () -> Unit,
    onPeriodClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Helper to format timestamps
    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val dateDisplay = if (startDate != null && endDate != null) {
        "${dateFormatter.format(Date(startDate))} - ${dateFormatter.format(Date(endDate))}"
    } else {
        val month = savingsGoal?.let { getMonthName(it.month) } ?: "Month"
        "01 $month - 30 $month"
    }
    val rawPercentage = if (savingsGoal != null && savingsGoal.targetAmount > 0) {
        (savingsGoal.currentSaved / savingsGoal.targetAmount) * 100
    } else {
        0.0
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saving Goal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                Surface(
                    onClick = onPeriodClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF9FAFB),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = dateDisplay, fontSize = 13.sp, color = Color(0xFF374151))
                        Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp).padding(start = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (savingsGoal != null) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        StatRow(Icons.Outlined.EmojiEvents, "Target Achieved", "$${String.format("%,.0f", savingsGoal.currentSaved)}")
                        Spacer(modifier = Modifier.height(24.dp))
                        StatRow(Icons.Outlined.AdsClick, "This month Target", "$${String.format("%,.0f", savingsGoal.targetAmount)}")
                    }

                    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                        SavingsGauge(
                            progress = rawPercentage.toFloat(), // Pass the 0-100 value directly now
                            targetAmount = savingsGoal?.targetAmount ?: 0.0
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.align(Alignment.CenterHorizontally).height(48.dp).padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF4B5563))
            ) {
                Text("Adjust Goal", color = Color(0xFF1F2937), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Edit, null, Modifier.size(16.dp), tint = Color.Black)
            }
        }
    }
}

@Composable
private fun StatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color(0xFF9CA3AF))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}