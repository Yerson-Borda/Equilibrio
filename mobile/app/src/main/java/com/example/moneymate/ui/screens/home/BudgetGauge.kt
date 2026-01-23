package com.example.moneymate.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BudgetGauge(
    progress: Float, // Should be between 0.0f and 1.0f (0% to 100%)
    spentAmount: Double,
    limitAmount: Double,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val hasLimit = limitAmount > 0
    val safeProgress = remember(progress) {
        progress.coerceIn(0f, 1f)
    }

    // Colors based on budget status
    val progressColor = when {
        safeProgress < 0.7f -> Color(0xFF4CAF50) // Green for normal
        safeProgress < 0.9f -> Color(0xFFFF9800) // Orange for warning
        else -> Color(0xFFF44336) // Red for critical/over budget
    }

    val trackColor = Color(0xFFF2F2F7)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // Fixed typo here
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Canvas(modifier = Modifier.size(100.dp, 70.dp)) {
                // Calculate dimensions (slightly smaller for home screen)
                val strokeWidth = 12.dp.toPx()
                val radius = size.width / 2 - strokeWidth
                val centerPoint = Offset(size.width / 2, size.height) // Bottom center

                // 1. Draw the Background Track
                drawArc(
                    color = trackColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(strokeWidth, size.height - radius)
                )

                // 2. Draw the Progress Arc
                if (safeProgress > 0) {
                    drawArc(
                        color = progressColor,
                        startAngle = 180f,
                        sweepAngle = 180f * safeProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(strokeWidth, size.height - radius)
                    )
                }

                // 3. Draw the Needle
                val angleRad = Math.toRadians((180f + (180f * safeProgress)).toDouble())
                val needleLength = radius - 4.dp.toPx()
                val endX = centerPoint.x + needleLength * cos(angleRad).toFloat()
                val endY = centerPoint.y + needleLength * sin(angleRad).toFloat()
                val endPoint = Offset(endX, endY)

                drawLine(
                    color = progressColor,
                    start = centerPoint,
                    end = endPoint,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Draw the pivot circle
                drawCircle(
                    color = progressColor,
                    radius = 5.dp.toPx(),
                    center = centerPoint
                )
            }

            // Percentage Text or Spent Amount
            Text(
                text = if (hasLimit) "${(safeProgress * 100).toInt()}%"
                else formatCurrency(spentAmount, currencySymbol),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Labels (0 and Limit/Spent)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", fontSize = 10.sp, color = Color.Gray)

            val formattedAmount = remember(limitAmount, spentAmount, hasLimit) {
                if (hasLimit) {
                    formatCurrency(limitAmount, currencySymbol)
                } else {
                    "No Limit"
                }
            }

            Text(
                text = formattedAmount,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

private fun formatCurrency(amount: Double, currencySymbol: String): String {
    return when {
        amount >= 1_000_000 -> String.format("%s%.1fM", currencySymbol, amount / 1_000_000)
        amount >= 1_000 -> String.format("%s%.1fK", currencySymbol, amount / 1_000)
        amount >= 100 -> String.format("%s%.0f", currencySymbol, amount)
        else -> String.format("%s%.2f", currencySymbol, amount)
    }
}