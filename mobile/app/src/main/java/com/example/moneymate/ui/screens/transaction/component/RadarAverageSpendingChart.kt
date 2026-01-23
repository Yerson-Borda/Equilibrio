package com.example.moneymate.ui.screens.transaction.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.transaction.model.AverageSpendingData
import com.example.domain.transaction.model.PeriodFilter
import kotlin.math.cos
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun RadarAverageSpendingChart(
    averageSpendingData: List<AverageSpendingData>,
    period: PeriodFilter,
    onPeriodChanged: (PeriodFilter) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(period) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Professional Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Spending Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = "Average spending per category",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            RadarPeriodDropdown(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { newPeriod ->
                    selectedPeriod = newPeriod
                    onPeriodChanged(newPeriod)
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(350.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF70C1B3), strokeWidth = 3.dp)
            }
        } else if (averageSpendingData.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(350.dp), contentAlignment = Alignment.Center) {
                Text("No data for this period", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
            }
        } else {
            RadarChart(
                spendingData = averageSpendingData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun RadarChart(
    spendingData: List<AverageSpendingData>,
    modifier: Modifier = Modifier
) {
    val filteredData = spendingData
        .filter { it.totalPeriodSpent > 0 }
        .sortedByDescending { it.totalPeriodSpent }
        .take(6) // 6 is the "sweet spot" for professional readability

    val totalSpent = filteredData.sumOf { it.totalPeriodSpent }
    val rawMax = filteredData.maxOfOrNull { it.totalPeriodSpent } ?: 1.0
    val maxScaleValue = (kotlin.math.ceil(rawMax / 10.0) * 10.0).coerceAtLeast(40.0)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val chartRadius = (minOf(size.width, size.height) / 2) * 0.65f

        // 1. Draw Subtle Polygonal Web
        drawProfessionalGrid(center, chartRadius, filteredData.size)

        // 2. Draw Subtle Scale Labels
        drawScaleNumbers(center, chartRadius, maxScaleValue)

        // 3. Draw Main Radar Data
        drawRadarDataArea(center, chartRadius, filteredData, maxScaleValue, totalSpent)
    }
}

private fun DrawScope.drawProfessionalGrid(center: Offset, maxRadius: Float, sides: Int) {
    val angleStep = (2 * Math.PI / sides).toFloat()
    val levels = 4 // Fewer levels = cleaner look

    for (i in 1..levels) {
        val radius = maxRadius * (i / levels.toFloat())
        val path = Path()
        for (j in 0 until sides) {
            val angle = j * angleStep - Math.PI / 2
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)

            // Draw axis lines on the outermost loop only
            if (i == levels) {
                drawLine(
                    color = Color(0xFFF0F0F0),
                    start = center,
                    end = Offset(x, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        path.close()
        drawPath(path = path, color = Color(0xFFE8E8E8), style = Stroke(width = 1.dp.toPx()))
    }
}

private fun DrawScope.drawScaleNumbers(center: Offset, maxRadius: Float, maxValue: Double) {
    val levels = 4
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.LTGRAY
        textSize = 10.sp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
    }

    for (i in 1..levels) {
        val radius = maxRadius * (i / levels.toFloat())
        val value = (maxValue * (i / levels.toFloat())).toInt()
        drawContext.canvas.nativeCanvas.drawText(value.toString(), center.x, center.y - radius - 8f, paint)
    }
}

private fun DrawScope.drawRadarDataArea(
    center: Offset,
    maxRadius: Float,
    data: List<AverageSpendingData>,
    maxValue: Double,
    totalSpent: Double
) {
    val angleStep = (2 * Math.PI / data.size).toFloat()
    val path = Path()
    val dataPoints = mutableListOf<Offset>()

    data.forEachIndexed { index, spending ->
        val normalizedValue = (spending.totalPeriodSpent / maxValue).toFloat().coerceAtMost(1.0f)
        val radius = maxRadius * normalizedValue
        val angle = index * angleStep - Math.PI / 2
        val point = Offset(
            center.x + radius * cos(angle).toFloat(),
            center.y + radius * sin(angle).toFloat()
        )
        dataPoints.add(point)
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()

    // Professional Sea-Green Theme
    val brandColor = Color(0xFF70C1B3)
    drawPath(path = path, color = brandColor.copy(alpha = 0.25f))
    drawPath(path = path, color = brandColor, style = Stroke(width = 2.5.dp.toPx(), join = StrokeJoin.Round))

    // Draw Points and Smart Labels
    data.forEachIndexed { index, spending ->
        val point = dataPoints[index]
        val angle = index * angleStep - Math.PI / 2
        val percentage = (spending.totalPeriodSpent / totalSpent * 100)

        // Point highlight
        drawCircle(color = brandColor, radius = 4.dp.toPx(), center = point)
        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = point)

        // Label Math
        val isRightSide = cos(angle) > 0
        val isBottomSide = sin(angle) > 0
        val labelOffset = 35.dp.toPx()

        val labelX = center.x + (maxRadius + labelOffset) * cos(angle).toFloat()
        val labelY = center.y + (maxRadius + (if (isBottomSide) 15.dp.toPx() else 5.dp.toPx())) * sin(angle).toFloat()

        drawContext.canvas.nativeCanvas.apply {
            // 1. Category Label
            drawText(
                spending.categoryName.uppercase(),
                labelX,
                labelY,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 11.sp.toPx()
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    textAlign = if (isRightSide) android.graphics.Paint.Align.LEFT else android.graphics.Paint.Align.RIGHT
                }
            )

            // 2. Percentage & Amount Sub-label
            drawText(
                "${String.format("%.1f", percentage)}% ($${spending.totalPeriodSpent.toInt()})",
                labelX,
                labelY + 16.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 10.sp.toPx()
                    textAlign = if (isRightSide) android.graphics.Paint.Align.LEFT else android.graphics.Paint.Align.RIGHT
                }
            )
        }
    }
}

@Composable
fun RadarPeriodDropdown(selectedPeriod: PeriodFilter, onPeriodSelected: (PeriodFilter) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true },
            color = Color(0xFFF8F9FA),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getRadarPeriodDisplayName(selectedPeriod),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.DarkGray
                )
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Gray
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White).clip(RoundedCornerShape(8.dp))
        ) {
            getRadarPeriodOptions().forEach { (period, label) ->
                DropdownMenuItem(
                    text = { Text(label, style = MaterialTheme.typography.bodyMedium) },
                    onClick = { onPeriodSelected(period); expanded = false }
                )
            }
        }
    }
}

private fun getRadarPeriodOptions() = listOf(
    PeriodFilter.MONTH to "This Month",
    PeriodFilter.DAYS_30 to "Last 30 Days",
    PeriodFilter.YEAR to "This Year"
)

private fun getRadarPeriodDisplayName(period: PeriodFilter) = when (period) {
    PeriodFilter.YEAR -> "Year"
    PeriodFilter.MONTH -> "Month"
    else -> "Select Period"
}