package com.example.moneymate.ui.screens.goal.component

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
fun SavingsGauge(progress: Float, targetAmount: Double) {
    val progressColor = Color(0xFF4F73FF)
    val trackColor = Color(0xFFE0E7FF)

    // FIX 1: Normalize the input.
    // If progress is > 1.0 (e.g. 50.0), assume it's a percentage and divide by 100.
    // If it's <= 1.0 (e.g. 0.5), use it as is.
    val normalizedProgress = remember(progress) {
        if (progress > 1f) progress / 100f else progress
    }

    // Clamp between 0.0 and 1.0 to prevent drawing errors
    val safeProgress = normalizedProgress.coerceIn(0f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Canvas(modifier = Modifier.size(120.dp, 80.dp)) {
                // Calculate dimensions
                val strokeWidth = 14.dp.toPx()
                val radius = size.width / 2 - strokeWidth
                val centerPoint = Offset(size.width / 2, size.height) // Bottom center

                // 1. Draw the Background Track (Gray/Light Blue)
                drawArc(
                    color = trackColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(strokeWidth, size.height - radius)
                )

                // 2. Draw the Progress Arc (Blue)
                // Only draw if there is progress > 0
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
                // 180 degrees = 0%, 360 degrees = 100%
                val angleRad = Math.toRadians((180f + (180f * safeProgress)).toDouble())

                // Make needle slightly shorter than radius so it fits nicely
                val needleLength = radius - 5.dp.toPx()

                val endX = centerPoint.x + needleLength * cos(angleRad).toFloat()
                val endY = centerPoint.y + needleLength * sin(angleRad).toFloat()
                val endPoint = Offset(endX, endY)

                drawLine(
                    color = progressColor,
                    start = centerPoint,
                    end = endPoint,
                    strokeWidth = 4.dp.toPx(), // Slightly thicker needle
                    cap = StrokeCap.Round
                )

                // Draw the pivot circle
                drawCircle(
                    color = progressColor,
                    radius = 6.dp.toPx(),
                    center = centerPoint
                )
            }

            // Percentage Text
            Text(
                text = "${(safeProgress * 100).toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 10.dp) // Lift text slightly
            )
        }

        // Labels (0 and Target)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$0", fontSize = 11.sp, color = Color.Gray)

            // FIX 2: Better formatting for target amount (e.g., 500 => 0.5k, 5000 => 5k)
            val formattedTarget = if (targetAmount >= 1000) {
                "${(targetAmount / 1000).toInt()}k"
            } else {
                "${targetAmount.toInt()}"
            }

            Text("$$formattedTarget", fontSize = 11.sp, color = Color.Gray)
        }
    }
}