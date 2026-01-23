package com.example.moneymate.ui.screens.goal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.goal.model.Goal
import com.example.moneymate.utils.Config
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
        // Enhanced header with image
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            AsyncImage(
                model = goal.image?.let { Config.buildImageUrl(it) } ?: "https://placehold.co/600x400/4A66FF/FFFFFF?text=${goal.title.replace(" ", "+")}",
                contentDescription = goal.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay for better text contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            // Top navigation row
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp), 
                Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack, 
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        null,
                        tint = Color.Black
                    )
                }
                IconButton(
                    onClick = { }, 
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert, 
                        null,
                        tint = Color.Black
                    )
                }
            }
            
            // Goal title at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = goal.title,
                    fontSize = 28.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.deadline?.toString() ?: "No deadline", 
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            }
        }

        // Content section
        Column(modifier = Modifier.padding(20.dp)) {
            // Description card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = goal.description ?: "No description provided",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Goal stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Goal Progress",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Progress numbers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$${goal.amountSaved.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A66FF)
                            )
                            Text(
                                text = "Saved",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "$${goal.goalAmount.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF374151)
                            )
                            Text(
                                text = "Target",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { goal.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape),
                        color = Color(0xFF4A66FF),
                        trackColor = Color(0xFFE5E7EB)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Progress percentage
                    Text(
                        text = "${(goal.progress * 100).toInt()}% completed",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A66FF),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    // Days remaining
                    goal.daysRemaining?.let { days ->
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${days} days remaining",
                                fontSize = 14.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Auto-deposit toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable auto-deposit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = "Automatically save towards your goal",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    Switch(
                        checked = false, 
                        onCheckedChange = {},
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4A66FF)
                        )
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Edit Goal")
                }
                
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A66FF))
                ) {
                    Text("Add Money", color = Color.White)
                }
            }
        }
    }
}