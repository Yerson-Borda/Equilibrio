package com.example.moneymate.ui.screens.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.categoryLimit.model.CategoryLimitOverview
import com.example.moneymate.ui.components.states.EmptyState
import com.example.moneymate.ui.components.states.FullScreenError
import com.example.moneymate.ui.components.states.FullScreenLoading
import com.example.moneymate.ui.navigation.BottomNavigationBar
import com.example.moneymate.ui.screens.goal.component.NotificationToggle
import com.example.moneymate.utils.IconMapper
import com.example.moneymate.utils.ScreenState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun GoalScreen(
    viewModel: GoalScreenViewModel = koinViewModel(),
    currentScreen: String = "goals",
    onNavigationItemSelected: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // State for budget editing dialogs
    var showEditMonthlyDialog by remember { mutableStateOf(false) }
    var showEditDailyDialog by remember { mutableStateOf(false) }
    var newMonthlyLimit by remember { mutableStateOf("") }
    var newDailyLimit by remember { mutableStateOf("") }

    // Handle budget errors
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error.getUserFriendlyMessage(),
                duration = SnackbarDuration.Short
            )
        }
    }

    // Handle category limit errors
    uiState.categoryLimitError?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearCategoryLimitError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFF121212),
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onNavigationItemSelected = onNavigationItemSelected
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState.budgetState) {
                is ScreenState.Loading -> {
                    FullScreenLoading(message = "Loading budget data...")
                }
                is ScreenState.Error -> {
                    FullScreenError(
                        error = state.error,
                        onRetry = { viewModel.loadBudgetData() }
                    )
                }
                is ScreenState.Success -> {
                    val budget = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Top Chart Section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E1E1E)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Charts Coming Soon",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Swipeable Container Section
                        SwipeableBudgetContainer(
                            budget = budget,
                            isNotificationsEnabled = uiState.isNotificationsEnabled,
                            onToggleNotifications = { viewModel.toggleNotifications() },
                            categoryLimitsState = uiState.categoryLimitsState,
                            overBudgetCategories = uiState.overBudgetCategories,
                            onUpdateCategoryLimit = { categoryId, limit ->
                                viewModel.updateCategoryLimit(categoryId, limit)
                            },
                            onDeleteCategoryLimit = { categoryId ->
                                viewModel.deleteCategoryLimit(categoryId)
                            },
                            isUpdatingCategoryLimit = uiState.isUpdatingCategoryLimit,
                            isDeletingCategoryLimit = uiState.isDeletingCategoryLimit,
                            onRefreshCategoryLimits = { viewModel.loadCategoryLimits() },
                            onEditMonthlyLimit = {
                                newMonthlyLimit = String.format("%.0f", budget.monthlyLimit)
                                showEditMonthlyDialog = true
                            },
                            onEditDailyLimit = {
                                newDailyLimit = String.format("%.0f", budget.dailyLimit)
                                showEditDailyDialog = true
                            },
                            isUpdatingBudget = uiState.isUpdating
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                is ScreenState.Empty -> {
                    EmptyState(
                        title = "No Budget Set",
                        message = "You haven't set up a budget yet. Create one to start tracking your spending!",
                        icon = androidx.compose.material.icons.Icons.Default.Payments,
                        action = {
                            Button(onClick = {
                                // TODO: Navigate to budget setup screen
                            }) {
                                Text("Set Up Budget")
                            }
                        }
                    )
                }
            }

            // Monthly Budget Edit Dialog
            if (showEditMonthlyDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showEditMonthlyDialog = false
                        newMonthlyLimit = ""
                    },
                    title = {
                        Text(
                            text = "Edit Monthly Budget",
                            color = Color.White
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Current monthly budget: $${String.format("%.0f", uiState.budget?.monthlyLimit ?: 0.0)}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            TextField(
                                value = newMonthlyLimit,
                                onValueChange = { newMonthlyLimit = it },
                                label = { Text("New Monthly Budget") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val limit = newMonthlyLimit.toDoubleOrNull()
                                if (limit != null) {
                                    viewModel.updateBudget(monthlyLimit = limit, dailyLimit = null)
                                    showEditMonthlyDialog = false
                                    newMonthlyLimit = ""
                                }
                            },
                            enabled = newMonthlyLimit.isNotBlank() && !uiState.isUpdating
                        ) {
                            if (uiState.isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showEditMonthlyDialog = false
                                newMonthlyLimit = ""
                            }
                        ) {
                            Text("Cancel")
                        }
                    },
                    containerColor = Color(0xFF1E1E1E)
                )
            }

            // Daily Limit Edit Dialog
            if (showEditDailyDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showEditDailyDialog = false
                        newDailyLimit = ""
                    },
                    title = {
                        Text(
                            text = "Edit Daily Limit",
                            color = Color.White
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Current daily limit: $${String.format("%.0f", uiState.budget?.dailyLimit ?: 0.0)}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            TextField(
                                value = newDailyLimit,
                                onValueChange = { newDailyLimit = it },
                                label = { Text("New Daily Limit") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val limit = newDailyLimit.toDoubleOrNull()
                                if (limit != null) {
                                    viewModel.updateBudget(monthlyLimit = null, dailyLimit = limit)
                                    showEditDailyDialog = false
                                    newDailyLimit = ""
                                }
                            },
                            enabled = newDailyLimit.isNotBlank() && !uiState.isUpdating
                        ) {
                            if (uiState.isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showEditDailyDialog = false
                                newDailyLimit = ""
                            }
                        ) {
                            Text("Cancel")
                        }
                    },
                    containerColor = Color(0xFF1E1E1E)
                )
            }
        }
    }
}

@Composable
fun BudgetAndLimitsPage(
    budget: com.example.domain.budget.model.Budget,
    isNotificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit,
    onEditMonthlyLimit: () -> Unit,
    onEditDailyLimit: () -> Unit,
    isUpdating: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Available Balance section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Available Balance",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$5,240.21",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "+2.5% vs Last month",
                    color = Color(0xFF4CAF50),
                    fontSize = 12.sp
                )
            }
        }

        // Separator line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.1f))
                .padding(bottom = 24.dp)
        )

        // Budget section (Monthly)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget ${getMonthName(budget.month)}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onEditMonthlyLimit,
                    enabled = !isUpdating,
                    modifier = Modifier.size(24.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Monthly Budget",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Budget",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = String.format("$%.0f", budget.monthlyLimit),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = String.format("$%.0f", budget.monthlySpent),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clip(RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(budget.monthlyProgress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .background(Color(0xFF4CAF50))
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }

        // Separator line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.1f))
                .padding(bottom = 24.dp)
        )

        // Daily Limit section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Limit",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onEditDailyLimit,
                    enabled = !isUpdating,
                    modifier = Modifier.size(24.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Daily Limit",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Limit",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = String.format("$%.0f", budget.dailyLimit),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = String.format("$%.0f", budget.dailySpent),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clip(RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(budget.dailyProgress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .background(Color(0xFF4CAF50))
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }

        // Separator line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.1f))
                .padding(bottom = 24.dp)
        )

        // Notification Toggle Section
        Column {
            Text(
                text = "Enable notifications?",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            NotificationToggle(
                isEnabled = isNotificationsEnabled,
                onToggle = onToggleNotifications,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LimitsPerCategoryPage(
    categoryLimitsState: ScreenState<List<CategoryLimitOverview>>,
    overBudgetCategories: List<CategoryLimitOverview>,
    onUpdateCategoryLimit: (Int, String) -> Unit,
    onDeleteCategoryLimit: (Int) -> Unit,
    isUpdatingCategoryLimit: Boolean = false,
    isDeletingCategoryLimit: Boolean = false,
    onRefreshCategoryLimits: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableIntStateOf(-1) }
    var selectedCategoryName by remember { mutableStateOf("") }
    var selectedCurrentLimit by remember { mutableStateOf("") }
    var newLimit by remember { mutableStateOf("") }

    val selectedCategory = remember(selectedCategoryId, categoryLimitsState) {
        when (categoryLimitsState) {
            is ScreenState.Success -> {
                categoryLimitsState.data.find { it.categoryId == selectedCategoryId }
            }
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp) // Reduced vertical padding
    ) {
        // Title and Refresh Button - MORE COMPACT
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp), // Reduced padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Category Limits",
                color = Color.White,
                fontSize = 16.sp, // Smaller
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { onRefreshCategoryLimits() },
                enabled = !isUpdatingCategoryLimit,
                modifier = Modifier.size(32.dp) // Smaller button
            ) {
                if (isUpdatingCategoryLimit) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp), // Smaller
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp) // Smaller
                    )
                }
            }
        }

        when (categoryLimitsState) {
            is ScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f), // Take available space
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            is ScreenState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f), // Take available space
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = { onRefreshCategoryLimits() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            is ScreenState.Success -> {
                val categoryLimits = categoryLimitsState.data

                if (categoryLimits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f), // Take available space
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "No limits",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(36.dp) // Smaller
                                    .padding(bottom = 12.dp)
                            )
                            Text(
                                text = "No Limits Set",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Set category spending limits",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    // Category Table Header - MORE COMPACT
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Category",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp, // Smaller
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "Budget",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp, // Smaller
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Remaining",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp, // Smaller
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Actions",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp, // Smaller
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Category Rows - MORE COMPACT
                    LazyColumn(
                        modifier = Modifier.weight(1f), // Take available space
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 20.dp,
                            vertical = 2.dp // Reduced
                        )
                    ) {
                        items(categoryLimits) { limit ->
                            val remaining = remember(limit) {
                                try {
                                    val limitAmount = limit.monthlyLimit.toDoubleOrNull() ?: 0.0
                                    val spentAmount = limit.monthlySpent.toDoubleOrNull() ?: 0.0
                                    limitAmount - spentAmount
                                } catch (e: Exception) {
                                    0.0
                                }
                            }

                            val isOverBudget = overBudgetCategories.any { it.categoryId == limit.categoryId }

                            CompactCategoryTableRow( // Use a more compact version
                                category = limit,
                                remaining = remaining,
                                isOverBudget = isOverBudget,
                                onEditClick = {
                                    selectedCategoryId = limit.categoryId
                                    selectedCategoryName = limit.categoryName
                                    selectedCurrentLimit = limit.monthlyLimit
                                    newLimit = limit.monthlyLimit
                                    showEditDialog = true
                                },
                                onDeleteClick = {
                                    selectedCategoryId = limit.categoryId
                                    selectedCategoryName = limit.categoryName
                                    showDeleteDialog = true
                                },
                                isDeleting = isDeletingCategoryLimit && selectedCategoryId == limit.categoryId
                            )

                            Spacer(modifier = Modifier.height(6.dp)) // Reduced
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp)) // Reduced
                }
            }

            is ScreenState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f), // Take available space
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Limits Set",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Set category spending limits",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Over-budget warning - MORE COMPACT
        if (overBudgetCategories.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp), // Reduced
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(6.dp) // Smaller
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp), // Reduced
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFF44336),
                        modifier = Modifier
                            .size(14.dp) // Smaller
                            .padding(end = 4.dp) // Reduced
                    )
                    Text(
                        text = "${overBudgetCategories.size} over budget",
                        color = Color(0xFFF44336),
                        fontSize = 11.sp // Smaller
                    )
                }
            }
        }

        // Details section - REMOVED or make optional
        // You can comment this out if there's not enough space
        /*
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp) // Reduced
        ) {
            Text(
                text = "Details",
                color = Color.White,
                fontSize = 13.sp, // Smaller
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp) // Reduced
            )

            Text(
                text = "Track spending with category limits",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp, // Smaller
                lineHeight = 14.sp
            )
        }
        */

        // Dialogs remain the same
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = {
                    showEditDialog = false
                    newLimit = ""
                },
                title = {
                    Text(
                        text = "Edit $selectedCategoryName Limit",
                        color = Color.White
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Current monthly limit: $selectedCurrentLimit",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        TextField(
                            value = newLimit,
                            onValueChange = { newLimit = it },
                            label = { Text("New Monthly Limit") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newLimit.isNotBlank() && selectedCategoryId != -1) {
                                onUpdateCategoryLimit(selectedCategoryId, newLimit)
                                showEditDialog = false
                                newLimit = ""
                            }
                        },
                        enabled = newLimit.isNotBlank() && !isUpdatingCategoryLimit
                    ) {
                        if (isUpdatingCategoryLimit) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEditDialog = false
                            newLimit = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }

        // Delete Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                },
                title = {
                    Text(
                        text = "Delete Limit",
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete the limit for $selectedCategoryName?",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteCategoryLimit(selectedCategoryId)
                            showDeleteDialog = false
                        },
                        enabled = !isDeletingCategoryLimit
                    ) {
                        if (isDeletingCategoryLimit) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Delete", color = Color(0xFFF44336))
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }
    }
}

// NEW: More compact category row
@Composable
fun CompactCategoryTableRow(
    category: CategoryLimitOverview,
    remaining: Double,
    isOverBudget: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDeleting: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            category.categoryIcon?.let { iconName ->
                val iconColor = IconMapper.parseColor(category.categoryColor)
                val backgroundColor = IconMapper.getBackgroundColor(category.categoryColor)

                Box(
                    modifier = Modifier
                        .size(28.dp) // Smaller
                        .background(
                            color = backgroundColor,
                            shape = RoundedCornerShape(6.dp) // Smaller
                        )
                        .padding(3.dp), // Reduced
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = IconMapper.getIconDrawable(iconName)),
                        contentDescription = category.categoryName,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp) // Smaller
                    )
                }

                Spacer(modifier = Modifier.width(8.dp)) // Reduced
            }

            Text(
                text = category.categoryName,
                color = Color.White,
                fontSize = 12.sp, // Smaller
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "$${category.monthlyLimit}",
            color = Color.White,
            fontSize = 12.sp, // Smaller
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (remaining >= 0) String.format("$%.0f", remaining) else String.format("-$%.0f", -remaining), // Remove decimals
            color = if (remaining >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontSize = 12.sp, // Smaller
            fontWeight = if (isOverBudget) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onEditClick,
                enabled = !isDeleting,
                modifier = Modifier.size(28.dp) // Smaller
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp) // Smaller
                )
            }

            IconButton(
                onClick = onDeleteClick,
                enabled = !isDeleting,
                modifier = Modifier.size(28.dp) // Smaller
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp), // Smaller
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(16.dp) // Smaller
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeableBudgetContainer(
    budget: com.example.domain.budget.model.Budget,
    isNotificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit,
    categoryLimitsState: ScreenState<List<CategoryLimitOverview>>,
    overBudgetCategories: List<CategoryLimitOverview>,
    onUpdateCategoryLimit: (Int, String) -> Unit,
    onDeleteCategoryLimit: (Int) -> Unit,
    isUpdatingCategoryLimit: Boolean,
    isDeletingCategoryLimit: Boolean,
    onRefreshCategoryLimits: () -> Unit,
    onEditMonthlyLimit: () -> Unit, // Add this
    onEditDailyLimit: () -> Unit,   // Add this
    isUpdatingBudget: Boolean = false // Add this
) {
    val pageCount = 2
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Title
        Text(
            text = "Budget and Limits",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Main container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp, max = 450.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF363A3F)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> BudgetAndLimitsPage(
                        budget = budget,
                        isNotificationsEnabled = isNotificationsEnabled,
                        onToggleNotifications = onToggleNotifications,
                        onEditMonthlyLimit = onEditMonthlyLimit, // Pass this
                        onEditDailyLimit = onEditDailyLimit,     // Pass this
                        isUpdating = isUpdatingBudget            // Pass this
                    )
                    1 -> LimitsPerCategoryPage(
                        categoryLimitsState = categoryLimitsState,
                        overBudgetCategories = overBudgetCategories,
                        onUpdateCategoryLimit = onUpdateCategoryLimit,
                        onDeleteCategoryLimit = onDeleteCategoryLimit,
                        isUpdatingCategoryLimit = isUpdatingCategoryLimit,
                        isDeletingCategoryLimit = isDeletingCategoryLimit,
                        onRefreshCategoryLimits = onRefreshCategoryLimits
                    )
                }
            }
        }

        // Swipe indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { page ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (page == pagerState.currentPage) {
                                Color.White
                            } else {
                                Color.White.copy(alpha = 0.3f)
                            }
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(page)
                            }
                        }
                )
            }
        }
    }
}

// Helper function
fun getMonthName(monthNumber: Int): String {
    return when (monthNumber) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> "Month $monthNumber"
    }
}