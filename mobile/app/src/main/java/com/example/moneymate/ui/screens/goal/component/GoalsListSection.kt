package com.example.moneymate.ui.components.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.goal.model.Goal
import com.example.moneymate.utils.ScreenState

@Composable
fun GoalsListSection(
    goalsState: ScreenState<List<Goal>>,
    onGoalClick: (Goal) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header with title and See All button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Spending Goals",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            SeeAllButton(onClick = onSeeAllClick)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Goals List
        when (goalsState) {
            is ScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading goals...",
                        color = Color.Black.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }

            is ScreenState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Failed to load goals",
                        color = Color.Black.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }

            is ScreenState.Success -> {
                val goals = goalsState.data
                if (goals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No goals yet",
                                color = Color.Black.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Create your first goal",
                                color = Color.Black.copy(alpha = 0.3f),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(goals) { goal ->
                            GoalCard(
                                goal = goal,
                                onClick = { onGoalClick(goal) }
                            )
                        }
                    }
                }
            }

            is ScreenState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No goals yet",
                            color = Color.Black.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Create your first goal",
                            color = Color.Black.copy(alpha = 0.3f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeeAllButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "See All",
            color = Color.Black.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "See All",
            tint = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.size(12.dp)
        )
    }
}