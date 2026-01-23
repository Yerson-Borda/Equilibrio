package com.example.moneymate.ui.screens.goal

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavController
import com.example.domain.budget.model.Budget
import com.example.domain.categoryLimit.model.CategoryLimitOverview
import com.example.domain.wallet.model.TotalBalance
import com.example.moneymate.R
import com.example.moneymate.ui.components.goal.GoalsListSection
import com.example.moneymate.ui.components.states.EmptyState
import com.example.moneymate.ui.components.states.FullScreenError
import com.example.moneymate.ui.components.states.FullScreenLoading
import com.example.moneymate.ui.components.states.SectionStateManager
import com.example.moneymate.ui.navigation.BottomNavigationBar
import com.example.moneymate.ui.screens.goal.component.NotificationToggle
import com.example.moneymate.ui.screens.goal.component.SavingSummaryChartSection
import com.example.moneymate.ui.screens.goal.component.SavingsGoalSection
import com.example.moneymate.utils.IconMapper
import com.example.moneymate.utils.ScreenState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun GoalScreen(
    navController: NavController? = null,
    viewModel: GoalScreenViewModel = koinViewModel(),
    currentScreen: String = "goals",
    onNavigationItemSelected: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formattedStartDate = remember(uiState.selectedStartDate) {
        uiState.selectedStartDate?.let { timestamp ->
            try {
                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            } catch (e: Exception) {
                "Start Date"
            }
        } ?: "Start Date"
    }

    val formattedEndDate = remember(uiState.selectedEndDate) {
        uiState.selectedEndDate?.let { timestamp ->
            try {
                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            } catch (e: Exception) {
                "End Date"
            }
        } ?: "End Date"
    }
    var showEditSavingsGoalDialog by remember { mutableStateOf(false) }
    var newSavingsGoalAmount by remember { mutableStateOf("") }

    // State for budget editing dialogs
    var showEditMonthlyDialog by remember { mutableStateOf(false) }
    var showEditDailyDialog by remember { mutableStateOf(false) }
    var newMonthlyLimit by remember { mutableStateOf("") }
    var newDailyLimit by remember { mutableStateOf("") }

    // This state is for the Material 3 Date Range Picker
    val dateRangePickerState = rememberDateRangePickerState()

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            Box(modifier = Modifier.statusBarsPadding()) {
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
                            contentDescription = "Goals",
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
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState.budgetState) {
                is ScreenState.Loading -> FullScreenLoading(message = "Loading...")
                is ScreenState.Error -> FullScreenError(error = state.error, onRetry = { viewModel.loadBudgetData() })
                is ScreenState.Success -> {
                    val budget = state.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Savings Goal
                        item {
                            SavingsGoalSection(
                                savingsGoal = uiState.savingsGoal,
                                isLoading = uiState.isSavingsGoalLoading,
                                isError = uiState.savingsGoalError != null,
                                startDate = formattedStartDate,
                                endDate = formattedEndDate,
                                onEditClick = {
                                    uiState.savingsGoal?.let { goal ->
                                        newSavingsGoalAmount = String.format("%.0f", goal.targetAmount)
                                        showEditSavingsGoalDialog = true
                                    }
                                },
                                onPeriodClick = { viewModel.toggleDateRangePicker(true) }
                            )
                        }

                        // 2. Summary Chart
                        item {
                            if (uiState.isSavingsTrendsLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF4D73FF))
                                }
                            } else if (uiState.savingsTrendsError != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "Error loading savings data",
                                                color = Color.Gray,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            TextButton(onClick = { viewModel.refreshOnScreenFocus() }) {
                                                Text("Retry")
                                            }
                                        }
                                    }
                                }
                            } else {
                                SavingSummaryChartSection(
                                    monthlyChartData = uiState.monthlyChartData,
                                    selectedMonth = uiState.selectedChartMonth,
                                    availableMonths = uiState.availableMonths,
                                    selectedPeriod = uiState.selectedPeriod,
                                    availablePeriods = uiState.availablePeriods,
                                    onMonthSelected = { month ->
                                        viewModel.onChartMonthSelected(month)
                                    },
                                    onPeriodSelected = { period ->
                                        viewModel.onPeriodSelected(period)
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        // 3. SWIPEABLE CONTAINER - Budget and Category Limits
                        item {
                            SwipeableBudgetContainer(
                                budget = budget,
                                balanceState = uiState.balanceState,
                                balanceComparison = uiState.balanceComparison,
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
                                isUpdatingBudget = uiState.isUpdating,
                                onRetryBalance = { viewModel.loadTotalBalance() },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // 4. NEW GOALS SECTION
                        item {
                            GoalsListSection(
                                goalsState = uiState.goalsState,
                                onGoalClick = { goal ->
                                    navController?.navigate("goalDetail/${goal.id}")
                                },
                                onSeeAllClick = {
                                    navController?.navigate("goalsList")
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
                is ScreenState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EmptyState(
                            title = "No Budget",
                            message = "Set up a budget.",
                            icon = Icons.Default.Payments
                        )

                        // Show goals section even when no budget
                        GoalsListSection(
                            goalsState = uiState.goalsState,
                            onGoalClick = { goal ->
                                navController?.navigate("goalDetail/${goal.id}")
                            },
                            onSeeAllClick = {
                                navController?.navigate("goalsList")
                            }
                        )
                    }
                }
            }

            // --- DATE RANGE PICKER DIALOG ---
            if (uiState.showDateRangePicker) {
                DatePickerDialog(
                    onDismissRequest = { viewModel.toggleDateRangePicker(false) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.onDateRangeSelected(
                                dateRangePickerState.selectedStartDateMillis,
                                dateRangePickerState.selectedEndDateMillis
                            )
                        }) { Text("Confirm") }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.toggleDateRangePicker(false) }) { Text("Cancel") }
                    }
                ) {
                    DateRangePicker(
                        state = dateRangePickerState,
                        modifier = Modifier.height(450.dp).padding(16.dp),
                        showModeToggle = false
                    )
                }
            }

            // --- EDIT SAVINGS GOAL DIALOG ---
            if (showEditSavingsGoalDialog) {
                AlertDialog(
                    onDismissRequest = { showEditSavingsGoalDialog = false },
                    title = { Text("Update Savings Goal") },
                    text = {
                        TextField(
                            value = newSavingsGoalAmount,
                            onValueChange = { newSavingsGoalAmount = it },
                            label = { Text("Target Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            newSavingsGoalAmount.toDoubleOrNull()?.let { viewModel.updateSavingsGoal(it) }
                            showEditSavingsGoalDialog = false
                        }) {
                            Text("Update")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditSavingsGoalDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // --- MONTHLY BUDGET EDIT DIALOG ---
            if (showEditMonthlyDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showEditMonthlyDialog = false
                        newMonthlyLimit = ""
                    },
                    title = {
                        Text(
                            text = "Edit Monthly Budget",
                            color = Color.Black
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Current monthly budget: $${String.format("%.0f", uiState.budget?.monthlyLimit ?: 0.0)}",
                                color = Color.Black.copy(alpha = 0.7f),
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
                                    viewModel.updateBudget(monthlyAmount = limit, dailyAmount = null)
                                    showEditMonthlyDialog = false
                                    newMonthlyLimit = ""
                                }
                            },
                            enabled = newMonthlyLimit.isNotBlank() && !uiState.isUpdating
                        ) {
                            if (uiState.isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp)
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
                    containerColor = Color.White
                )
            }

            // --- DAILY LIMIT EDIT DIALOG ---
            if (showEditDailyDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showEditDailyDialog = false
                        newDailyLimit = ""
                    },
                    title = {
                        Text(
                            text = "Edit Daily Limit",
                            color = Color.Black
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Current daily limit: $${String.format("%.0f", uiState.budget?.dailyLimit ?: 0.0)}",
                                color = Color.Black.copy(alpha = 0.7f),
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
                                    viewModel.updateBudget(monthlyAmount = null, dailyAmount = limit)
                                    showEditDailyDialog = false
                                    newDailyLimit = ""
                                }
                            },
                            enabled = newDailyLimit.isNotBlank() && !uiState.isUpdating
                        ) {
                            if (uiState.isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
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
                    containerColor = Color.White
                )
            }
        }
    }
}

/**
 * HELPER FUNCTION: Corrects the "Unresolved reference: getMonthName"
 */
fun getMonthName(month: Int): String {
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return if (month in 1..12) months[month - 1] else "Month"
}

@Composable
fun BudgetAndLimitsPage(
    budget: Budget,
    balanceState: ScreenState<TotalBalance?>,
    balanceComparison: String,
    isNotificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit,
    onEditMonthlyLimit: () -> Unit,
    onEditDailyLimit: () -> Unit,
    isUpdating: Boolean = false,
    onRetryBalance: () -> Unit
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
                color = Color.Black.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionStateManager(
                    state = balanceState,
                    onRetry = { onRetryBalance() }
                ) { balance ->
                    Text(
                        text = "$${balance?.totalBalance ?: "0.00"}",
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = balanceComparison,
                    color = Color(0xFF4CAF50),
                    fontSize = 12.sp
                )
            }

            // Warning for exceeded limits
            val exceededLimits = mutableListOf<String>()
            if (budget.isMonthlyExceeded) {
                exceededLimits.add("Monthly budget")
            }
            if (budget.isDailyExceeded) {
                exceededLimits.add("Daily limit")
            }

            if (exceededLimits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Exceeded: ${exceededLimits.joinToString(", ")}",
                        color = Color(0xFFF44336),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Separator line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black.copy(alpha = 0.1f))
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
                    color = Color.Black,
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
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Monthly Budget",
                            tint = Color.Black.copy(alpha = 0.7f),
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
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = String.format("$%.0f", budget.monthlyLimit),
                    color = Color.Black,
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
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = String.format("$%.0f", budget.monthlySpent),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.Black.copy(alpha = 0.1f))
                    .clip(RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(budget.monthlyProgress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .background(if (budget.isMonthlyExceeded) Color(0xFFF44336) else Color(0xFF4CAF50))
                        .clip(RoundedCornerShape(2.dp))
                )
            }

            // Warning text when exceeded
            if (budget.isMonthlyExceeded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ You've exceeded your monthly budget by $${String.format("%.2f", -budget.monthlyRemaining)}",
                    color = Color(0xFFF44336),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Separator line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black.copy(alpha = 0.1f))
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
                    color = Color.Black,
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
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Daily Limit",
                            tint = Color.Black.copy(alpha = 0.7f),
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
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = String.format("$%.0f", budget.dailyLimit),
                    color = Color.Black,
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
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = String.format("$%.0f", budget.dailySpent),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.Black.copy(alpha = 0.1f))
                    .clip(RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(budget.dailyProgress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .background(if (budget.isDailyExceeded) Color(0xFFF44336) else Color(0xFF4CAF50))
                        .clip(RoundedCornerShape(2.dp))
                )
            }

            // Warning text when exceeded
            if (budget.isDailyExceeded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ You've exceeded your daily limit by $${String.format("%.2f", -budget.dailyRemaining)}",
                    color = Color(0xFFF44336),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Separator line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black.copy(alpha = 0.1f))
                .padding(bottom = 24.dp)
        )

        // Notification Toggle Section
        Column {
            Text(
                text = "Enable notifications?",
                color = Color.Black.copy(alpha = 0.7f),
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
    ) {
        // Title and Refresh Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Category Limits",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Warning for over-budget categories
                if (overBudgetCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${overBudgetCategories.size} categories over budget",
                            color = Color(0xFFF44336),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            IconButton(
                onClick = { onRefreshCategoryLimits() },
                enabled = !isUpdatingCategoryLimit,
                modifier = Modifier.size(32.dp)
            ) {
                if (isUpdatingCategoryLimit) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        when (categoryLimitsState) {
            is ScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Black)
                }
            }

            is ScreenState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load",
                            color = Color.Black.copy(alpha = 0.7f),
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
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "No limits",
                                tint = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(bottom = 12.dp)
                            )
                            Text(
                                text = "No Limits Set",
                                color = Color.Black.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Set category spending limits",
                                color = Color.Black.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    // Category Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Category",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "Budget",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Remaining",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Actions",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Category Rows
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = 20.dp,
                            vertical = 2.dp
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

                            CompactCategoryTableRow(
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

                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            is ScreenState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Limits Set",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Set category spending limits",
                            color = Color.Black.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Over-budget warning
        if (overBudgetCategories.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFF44336),
                        modifier = Modifier
                            .size(14.dp)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = "${overBudgetCategories.size} over budget",
                        color = Color(0xFFF44336),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- CATEGORY LIMIT EDIT DIALOG ---
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = {
                    showEditDialog = false
                    newLimit = ""
                },
                title = {
                    Text(
                        text = "Edit ${selectedCategoryName} Limit",
                        color = Color.Black
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Current limit: $$selectedCurrentLimit",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        TextField(
                            value = newLimit,
                            onValueChange = { newLimit = it },
                            label = { Text("New Monthly Limit") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newLimit.isNotBlank()) {
                                onUpdateCategoryLimit(selectedCategoryId, newLimit)
                                showEditDialog = false
                                newLimit = ""
                            }
                        },
                        enabled = newLimit.isNotBlank() && !isUpdatingCategoryLimit
                    ) {
                        if (isUpdatingCategoryLimit && selectedCategoryId == selectedCategoryId) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Black,
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
                containerColor = Color.White
            )
        }
    }
}

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
                        .size(28.dp)
                        .background(
                            color = backgroundColor,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = IconMapper.getIconDrawable(iconName)),
                        contentDescription = category.categoryName,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.categoryName,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isOverBudget) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Over budget",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                if (isOverBudget) {
                    Text(
                        text = "⚠️ Over budget",
                        color = Color(0xFFF44336),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Text(
            text = "$${category.monthlyLimit}",
            color = Color.Black,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (remaining >= 0) String.format("$%.0f", remaining) else String.format("-$%.0f", -remaining),
            color = if (remaining >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontSize = 12.sp,
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
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onDeleteClick,
                enabled = !isDeleting,
                modifier = Modifier.size(28.dp)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeableBudgetContainer(
    budget: Budget,
    balanceState: ScreenState<TotalBalance?>,
    balanceComparison: String,
    isNotificationsEnabled: Boolean,
    onToggleNotifications: () -> Unit,
    categoryLimitsState: ScreenState<List<CategoryLimitOverview>>,
    overBudgetCategories: List<CategoryLimitOverview>,
    onUpdateCategoryLimit: (Int, String) -> Unit,
    onDeleteCategoryLimit: (Int) -> Unit,
    isUpdatingCategoryLimit: Boolean,
    isDeletingCategoryLimit: Boolean,
    onRefreshCategoryLimits: () -> Unit,
    onEditMonthlyLimit: () -> Unit,
    onEditDailyLimit: () -> Unit,
    isUpdatingBudget: Boolean = false,
    onRetryBalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pageCount = 2
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Title
        Text(
            text = "Budget and Limits",
            color = Color.Black,
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
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> BudgetAndLimitsPage(
                        budget = budget,
                        balanceState = balanceState,
                        balanceComparison = balanceComparison,
                        isNotificationsEnabled = isNotificationsEnabled,
                        onToggleNotifications = onToggleNotifications,
                        onEditMonthlyLimit = onEditMonthlyLimit,
                        onEditDailyLimit = onEditDailyLimit,
                        isUpdating = isUpdatingBudget,
                        onRetryBalance = onRetryBalance
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
                                Color.Black
                            } else {
                                Color.Black.copy(alpha = 0.3f)
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