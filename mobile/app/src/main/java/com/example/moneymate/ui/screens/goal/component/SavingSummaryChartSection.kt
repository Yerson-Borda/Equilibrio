package com.example.moneymate.ui.screens.goal.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.axis.AxisData
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.example.domain.transaction.model.MonthlyChartData
import com.example.moneymate.ui.screens.transaction.chart.YChartLineDataConverter

@Composable
fun SavingSummaryChartSection(
    monthlyChartData: MonthlyChartData?,
    selectedMonth: String,
    availableMonths: List<String>,
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saving Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Box {
                    Text(
                        text = "$selectedMonth ▾",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(4.dp)
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableMonths.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month) },
                                onClick = {
                                    onMonthSelected(month)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (monthlyChartData != null) {
                val points = YChartLineDataConverter.convertToIncomePoints(monthlyChartData.days)

                if (points.isNotEmpty()) {
                    val xAxisData = AxisData.Builder()
                        .axisStepSize(40.dp)
                        .steps(points.size - 1)
                        .labelData { i ->
                            if (i < monthlyChartData.days.size) {
                                monthlyChartData.days[i].date.takeLast(2)
                            } else ""
                        }
                        .axisLabelColor(Color.LightGray)
                        .axisLineColor(Color.Transparent)
                        .backgroundColor(Color.White)
                        .build()

                    val yAxisData = AxisData.Builder()
                        .steps(4)
                        .labelData { i -> "${(i * 20)}k" }
                        .axisLabelColor(Color.LightGray)
                        .axisLineColor(Color.Transparent)
                        .backgroundColor(Color.White)
                        .build()

                    LineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        lineChartData = LineChartData(
                            linePlotData = LinePlotData(lines = listOf(Line(
                                dataPoints = points,
                                lineStyle = LineStyle(color = Color(0xFF4D73FF), lineType = LineType.SmoothCurve(false)),
                                intersectionPoint = IntersectionPoint(color = Color(0xFF4D73FF)),
                                selectionHighlightPoint = SelectionHighlightPoint(),
                                shadowUnderLine = ShadowUnderLine(alpha = 0.1f, color = Color(0xFF4D73FF)),
                                selectionHighlightPopUp = SelectionHighlightPopUp()
                            ))),
                            xAxisData = xAxisData,
                            yAxisData = yAxisData,
                            gridLines = GridLines(color = Color(0xFFF0F0F0)),
                            backgroundColor = Color.White
                        )
                    )
                } else {
                    Box(Modifier.height(180.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No data for this month", color = Color.Gray)
                    }
                }
            } else {
                Box(Modifier.height(180.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4D73FF))
                }
            }
        }
    }
}