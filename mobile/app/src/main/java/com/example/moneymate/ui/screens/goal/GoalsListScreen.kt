package com.example.moneymate.ui.screens.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.domain.goal.model.Goal
import com.example.moneymate.R
import com.example.moneymate.ui.components.states.EmptyState
import com.example.moneymate.ui.components.states.FullScreenError
import com.example.moneymate.ui.components.states.FullScreenLoading
import com.example.moneymate.ui.navigation.BottomNavigationBar
import com.example.moneymate.utils.ScreenState
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsListScreen(
    navController: NavController,
    viewModel: GoalScreenViewModel = koinViewModel(),
    currentScreen: String = "goals",
    onNavigationItemSelected: (String) -> Unit = {},
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load goals when screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadGoals()
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            Box(modifier = Modifier.statusBarsPadding()) { // Wrap in Box with status bar padding
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_arrow),
                            contentDescription = "goals",
                            tint = Color.Black,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(105.dp))
                    Text(
                        text = "goals",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onNavigationItemSelected = onNavigationItemSelected
            )
        },
        floatingActionButton = {
            AddRecordButton(
                onClick = {
                    // Navigate to create goal screen
                    navController.navigate("createGoal")
                },
                iconRes = R.drawable.add_outline,
                contentDescription = "Add Goal",
                modifier = Modifier.padding(bottom = 70.dp) // Adjust padding to avoid overlap with bottom bar
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState.goalsState) {
                is ScreenState.Loading -> FullScreenLoading(message = "Loading goals...")
                is ScreenState.Error -> FullScreenError(error = state.error, onRetry = { viewModel.loadGoals() })
                is ScreenState.Success -> {
                    val goals = state.data

                    if (goals.isEmpty()) {
                        EmptyState(
                            title = "No Goals",
                            message = "Create your first spending goal",
                            icon = Icons.Default.Add
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 16.dp,
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 80.dp // Extra padding for bottom bar
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(goals) { goal ->
                                VerticalGoalCard(
                                    goal = goal,
                                    onClick = {
                                        // Navigate to goal detail
                                        navController.navigate("goalDetail/${goal.id}")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                is ScreenState.Empty -> {
                    EmptyState(
                        title = "No Goals",
                        message = "Create your first spending goal",
                        icon = Icons.Default.Add
                    )
                }
            }
        }
    }
}

@Composable
fun VerticalGoalCard(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Goal Title
            Text(
                text = goal.title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Deadline
            if (goal.deadline != null) {
                Text(
                    text = formatDate(goal.deadline!!),
                    color = Color.Black.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Progress Section
            Column {
                // Amount saved vs target
                Text(
                    text = "$${formatAmount(goal.amountSaved)} out of $${formatAmount(goal.goalAmount)}",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                ProgressIndicator(
                    progress = goal.progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(goal.progress * 100).toInt()}%",
                        color = Color.Black.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )

                    if (goal.isAchieved) {
                        Text(
                            text = "Achieved!",
                            color = Color(0xFF4CAF50),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        goal.daysRemaining?.let { days ->
                            Text(
                                text = "${days} days left",
                                color = Color(0xFFFF9800),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Color.Black.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                    )
                )
        )
    }
}

@Composable
fun AddRecordButton(
    onClick: () -> Unit,
    iconRes: Int,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF4D6BFA),
    size: Int = 56,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return date.format(formatter)
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 1000) {
        String.format("%.1fk", amount / 1000)
    } else {
        String.format("%.0f", amount)
    }
}