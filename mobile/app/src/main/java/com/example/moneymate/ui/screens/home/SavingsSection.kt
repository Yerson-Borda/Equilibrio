//package com.example.moneymate.ui.screens.home
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@Composable
//fun SavingsSection(savingsGoals: List<SavingsGoal>) {
//    Column {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Savings",
//                color = Color(0xFF1E1E1E),
//                fontSize = 18.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            if (savingsGoals.isNotEmpty()) {
//                Text(
//                    text = "${savingsGoals.size} goals",
//                    color = Color(0xFF666666),
//                    fontSize = 14.sp
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        if (savingsGoals.isEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(
//                        color = Color(0xFF1E1E1E),
//                        shape = RoundedCornerShape(12.dp)
//                    )
//                    .padding(24.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "No savings goals yet",
//                    color = Color(0xFFAAAAAA),
//                    fontSize = 14.sp
//                )
//            }
//        } else {
//            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                savingsGoals.take(3).forEach { goal -> // Show only first 3 goals
//                    SavingsGoalRow(goal)
//                }
//
//                if (savingsGoals.size > 3) {
//                    Text(
//                        text = "+${savingsGoals.size - 3} more goals",
//                        color = Color(0xFF2196F3),
//                        fontSize = 14.sp,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { /* Navigate to full savings screen */ }
//                            .padding(8.dp),
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SavingsGoalRow(goal: SavingsGoal) {
//    val progress = if (goal.targetAmount > 0) {
//        (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat() // Convert to Float
//    } else {
//        0f
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(
//                color = Color(0xFF1E1E1E),
//                shape = RoundedCornerShape(12.dp)
//            )
//            .padding(16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Column(modifier = Modifier.weight(1f)) {
//            Text(
//                text = goal.name,
//                color = Color.White,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            // Progress bar
//            LinearProgressIndicator(
//                progress = progress,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(6.dp),
//                color = Color(0xFF2196F3),
//                trackColor = Color(0xFF333333)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = "$${"%.2f".format(goal.currentAmount)} of $${"%.2f".format(goal.targetAmount)}",
//                color = Color(0xFFAAAAAA),
//                fontSize = 12.sp
//            )
//        }
//
//        Spacer(modifier = Modifier.width(16.dp))
//
//        Text(
//            text = "${(progress * 100).toInt()}%",
//            color = Color(0xFF2196F3),
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}