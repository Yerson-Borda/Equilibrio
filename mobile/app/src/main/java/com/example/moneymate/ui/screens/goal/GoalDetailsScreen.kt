package com.example.moneymate.ui.screens.goal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.goal.model.Goal
import com.example.moneymate.utils.ScreenState
import org.koin.androidx.compose.koinViewModel

@Composable
fun GoalDetailsScreen(
    viewModel: GoalDetailViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(containerColor = Color.White) { paddingValues ->
        // Apply paddingValues to the root container
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val s = state) {
                is ScreenState.Loading -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is ScreenState.Success -> GoalDetailContent(s.data, onBack)
                is ScreenState.Error -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(s.error.getUserFriendlyMessage())
                    Button(onClick = { s.retryAction?.invoke() }) { Text("Retry") }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun GoalDetailContent(goal: Goal, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            AsyncImage(
                model = goal.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, Modifier.background(Color.White.copy(0.7f), CircleShape)) {
                    Icon(Icons.Default.ArrowBack, null)
                }
                IconButton(onClick = { }, Modifier.background(Color.White.copy(0.7f), CircleShape)) {
                    Icon(Icons.Default.MoreVert, null)
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text(goal.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(goal.deadline?.toString() ?: "No deadline", color = Color.Gray)

            Spacer(Modifier.height(16.dp))
            Text(goal.description ?: "", style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider(Modifier.padding(vertical = 20.dp), thickness = 0.5.dp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable autodeposit?", fontSize = 16.sp)
                Icon(Icons.Default.Info, null, Modifier.size(16.dp).padding(start = 4.dp), Color.Gray)
                Spacer(Modifier.weight(1f))
                Switch(checked = false, onCheckedChange = {})
            }

            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Wallet", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Text("T-Банк", fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text("Amount", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Text("${goal.currency} 100", fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                }
                Icon(Icons.Default.DateRange, null)
            }

            Spacer(Modifier.height(30.dp))

            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F5F5)).padding(12.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = Color.Gray)
                Spacer(Modifier.width(8.dp))
                Text("At the current rate, you'll reach your ${goal.goalAmount} goal by May 2032. Consider increasing deposit to $533.", fontSize = 12.sp)
            }

            Spacer(Modifier.height(30.dp))

            Text("${goal.amountSaved.toInt()} out of ${goal.goalAmount.toInt()}", Modifier.align(Alignment.End), fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = Color(0xFF4A66FF),
                trackColor = Color(0xFFE0E5FF)
            )
        }
    }
}