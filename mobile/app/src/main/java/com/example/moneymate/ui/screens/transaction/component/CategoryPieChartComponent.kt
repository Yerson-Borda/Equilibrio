package com.example.moneymate.ui.screens.transaction.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.transaction.model.CategorySummaryData

@Composable
fun CategoryPieChartComponent(
    categorySummaryData: CategorySummaryData,
    modifier: Modifier = Modifier
) {
    val categories = categorySummaryData.expenses
    val totalAmount = categorySummaryData.totalExpenses

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Expense Distribution",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                if (categories.isNotEmpty() && totalAmount > 0) {
                    PieChart(
                        categories = categories,
                        totalAmount = totalAmount,
                        modifier = Modifier.size(180.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "$${String.format("%,.0f", totalAmount)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                } else {
                    Text(
                        text = "No expense data",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                categories.forEachIndexed { index, category ->
                    CategoryListItem(
                        category = category,
                        totalAmount = totalAmount,
                        color = getCategoryColor(index),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PieChart(
    categories: List<com.example.domain.transaction.model.CategoryData>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2
        var startAngle = -90f

        categories.forEachIndexed { index, category ->
            val sweepAngle = (category.totalAmount / totalAmount * 360).toFloat()

            drawArc(
                color = getCategoryColor(index),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            startAngle += sweepAngle
        }

        drawCircle(
            color = Color.White,
            radius = radius * 0.5f,
            center = center
        )
    }
}

@Composable
private fun CategoryListItem(
    category: com.example.domain.transaction.model.CategoryData,
    totalAmount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalAmount > 0) {
        (category.totalAmount / totalAmount * 100)
    } else {
        0.0
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = category.categoryName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        Text(
            text = "${String.format("%.0f", percentage)}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

private fun getCategoryColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF4ECDC4), // Teal - Bills & Utilities
        Color(0xFFFF6B6B), // Red - Food
        Color(0xFF45B7D1), // Blue - Personal
        Color(0xFF96CEB4), // Green - Healthcare
        Color(0xFFFECA57), // Yellow - Education
        Color(0xFFFF9FF3), // Pink - Transport
        Color(0xFF54A0FF), // Light Blue - Investment
        Color(0xFF5F27CD), // Purple - Other
        Color(0xFF00D2D3), // Cyan
        Color(0xFFFF9F43)  // Orange
    )
    return colors.getOrNull(index % colors.size) ?: Color(0xFFCCCCCC)
}