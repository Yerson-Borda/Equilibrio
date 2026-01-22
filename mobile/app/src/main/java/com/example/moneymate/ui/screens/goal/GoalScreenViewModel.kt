package com.example.moneymate.ui.screens.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.budget.model.Budget
import com.example.domain.budget.usecase.GetCurrentBudgetUseCase
import com.example.domain.budget.usecase.UpdateBudgetUseCase
import com.example.domain.categoryLimit.model.CategoryLimitOverview
import com.example.domain.categoryLimit.usecase.DeleteCategoryLimitUseCase
import com.example.domain.categoryLimit.usecase.GetCategoryLimitsUseCase
import com.example.domain.categoryLimit.usecase.UpdateCategoryLimitUseCase
import com.example.domain.goal.model.Goal
import com.example.domain.goal.usecase.GetGoalsUseCase // Add this import
import com.example.domain.savingsGoal.model.SavingsGoal
import com.example.domain.savingsGoal.usecase.GetCurrentSavingsGoalUseCase
import com.example.domain.savingsGoal.usecase.UpdateSavingsGoalUseCase
import com.example.domain.transaction.model.MonthlyChartData
import com.example.domain.transaction.usecase.GetMonthlyChartDataUseCase
import com.example.moneymate.utils.DataSyncManager
import com.example.moneymate.utils.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalScreenViewModel(
    private val getCurrentBudgetUseCase: GetCurrentBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val getCategoryLimitsUseCase: GetCategoryLimitsUseCase,
    private val updateCategoryLimitUseCase: UpdateCategoryLimitUseCase,
    private val deleteCategoryLimitUseCase: DeleteCategoryLimitUseCase,
    private val getCurrentSavingsGoalUseCase: GetCurrentSavingsGoalUseCase,
    private val updateSavingsGoalUseCase: UpdateSavingsGoalUseCase,
    private val getMonthlyChartDataUseCase: GetMonthlyChartDataUseCase,
    private val getGoalsUseCase: GetGoalsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalScreenState())
    val uiState: StateFlow<GoalScreenState> = _uiState.asStateFlow()

    init {
        loadAllData()
        setupDataChangeListener()
    }

    private fun loadAllData() {
        loadBudgetData()
        loadCategoryLimits()
        loadSavingsGoal()
        loadChartData()
        loadGoals() // Load goals when screen initializes
    }

    private fun setupDataChangeListener() {
        viewModelScope.launch {
            DataSyncManager.dataChangeEvents.collect { event ->
                when (event) {
                    is DataSyncManager.DataChangeEvent.BudgetUpdated,
                    is DataSyncManager.DataChangeEvent.TransactionsUpdated -> {
                        loadBudgetData()
                        loadChartData()
                        loadSavingsGoal() // Refresh savings when transactions change
                    }
                    DataSyncManager.DataChangeEvent.CategoryLimitsUpdated -> {
                        loadCategoryLimits()
                    }
                    DataSyncManager.DataChangeEvent.GoalsUpdated -> {
                        loadGoals() // Reload goals when they change
                    }
                    else -> Unit
                }
            }
        }
    }

    // --- GOALS FUNCTIONS ---
    fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(goalsState = ScreenState.Loading) }
            try {
                val result = getGoalsUseCase()
                if (result.isSuccess) {
                    val goals = result.getOrThrow()
                    _uiState.update {
                        it.copy(
                            goalsState = if (goals.isEmpty()) ScreenState.Empty else ScreenState.Success(goals),
                            goals = goals
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to load goals")
                    _uiState.update {
                        it.copy(
                            goalsState = ScreenState.Error(
                                com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception),
                                retryAction = { loadGoals() }
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        goalsState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                            retryAction = { loadGoals() }
                        )
                    )
                }
            }
        }
    }

    // --- UI INTERACTION FUNCTIONS ---

    fun toggleNotifications() {
        _uiState.update { it.copy(isNotificationsEnabled = !it.isNotificationsEnabled) }
    }

    fun toggleDateRangePicker(show: Boolean) {
        _uiState.update { it.copy(showDateRangePicker = show) }
    }

    fun onDateRangeSelected(startDate: Long?, endDate: Long?) {
        _uiState.update {
            it.copy(
                selectedStartDate = startDate,
                selectedEndDate = endDate,
                showDateRangePicker = false
            )
        }
    }

    fun onChartMonthSelected(month: String) {
        _uiState.update { it.copy(selectedChartMonth = month) }
        loadChartData()
    }

    // --- DATA LOADING LOGIC ---

    fun loadChartData() {
        viewModelScope.launch {
            try {
                val result = getMonthlyChartDataUseCase.execute(months = 12)
                _uiState.update { it.copy(monthlyChartData = result) }
            } catch (e: Exception) {
                println("DEBUG: Failed to load chart data: ${e.message}")
            }
        }
    }

    fun loadBudgetData() {
        viewModelScope.launch {
            _uiState.update { it.copy(budgetState = ScreenState.Loading) }
            try {
                val result = getCurrentBudgetUseCase()
                if (result.isSuccess) {
                    val budgetData = result.getOrThrow()
                    _uiState.update {
                        it.copy(
                            budgetState = ScreenState.Success(budgetData),
                            budget = budgetData
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to load budget")
                    _uiState.update {
                        it.copy(
                            budgetState = ScreenState.Error(
                                com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception),
                                retryAction = { loadBudgetData() }
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        budgetState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                            retryAction = { loadBudgetData() }
                        )
                    )
                }
            }
        }
    }

    fun updateBudget(monthlyAmount: Double, dailyAmount: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                val result = updateBudgetUseCase(monthlyAmount, dailyAmount)
                if (result.isSuccess) {
                    val updatedBudget = result.getOrThrow()
                    _uiState.update {
                        it.copy(
                            budgetState = ScreenState.Success(updatedBudget),
                            budget = updatedBudget,
                            isUpdating = false
                        )
                    }
                    // Notify other screens about budget update
                    DataSyncManager.notifyBudgetUpdated()
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to update budget")
                    _uiState.update {
                        it.copy(
                            isUpdating = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUpdating = false) }
            }
        }
    }

    fun loadSavingsGoal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingsGoalLoading = true) }
            try {
                val result = getCurrentSavingsGoalUseCase()
                if (result.isSuccess) {
                    val goal = result.getOrThrow()
                    _uiState.update {
                        it.copy(
                            savingsGoal = goal,
                            isSavingsGoalLoading = false,
                            // Set total balance to current saved amount for real-time card updates
                            totalBalance = goal.currentSaved
                        )
                    }
                } else {
                    _uiState.update { it.copy(isSavingsGoalLoading = false, savingsGoalError = "Failed to load goal") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingsGoalLoading = false, savingsGoalError = e.message) }
            }
        }
    }

    fun updateSavingsGoal(targetAmount: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingsGoalUpdating = true) }
            try {
                val result = updateSavingsGoalUseCase(targetAmount)
                if (result.isSuccess) {
                    _uiState.update { it.copy(savingsGoal = result.getOrThrow(), isSavingsGoalUpdating = false) }
                } else {
                    _uiState.update { it.copy(isSavingsGoalUpdating = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingsGoalUpdating = false) }
            }
        }
    }

    // --- CATEGORY LIMITS LOGIC ---

    fun loadCategoryLimits() {
        viewModelScope.launch {
            _uiState.update { it.copy(categoryLimitsState = ScreenState.Loading, isCategoryLimitsLoading = true) }
            try {
                val result = getCategoryLimitsUseCase()
                if (result.isSuccess) {
                    val categoryLimits = result.getOrThrow()
                    _uiState.update {
                        it.copy(
                            categoryLimitsState = ScreenState.Success(categoryLimits),
                            overBudgetCategories = calculateOverBudgetCategories(categoryLimits),
                            isCategoryLimitsLoading = false,
                            isUpdatingCategoryLimit = false,
                            isDeletingCategoryLimit = false
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to load category limits")
                    _uiState.update {
                        it.copy(
                            categoryLimitsState = ScreenState.Error(
                                com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception),
                                retryAction = { loadCategoryLimits() }
                            ),
                            isCategoryLimitsLoading = false,
                            isUpdatingCategoryLimit = false,
                            isDeletingCategoryLimit = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        categoryLimitsState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                            retryAction = { loadCategoryLimits() }
                        ),
                        isCategoryLimitsLoading = false,
                        isUpdatingCategoryLimit = false,
                        isDeletingCategoryLimit = false
                    )
                }
            }
        }
    }

    fun updateCategoryLimit(categoryId: Int, monthlyLimit: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingCategoryLimit = true) }
            try {
                val result = updateCategoryLimitUseCase(categoryId, monthlyLimit)
                if (result.isSuccess) {
                    // Refresh the category limits list
                    loadCategoryLimits()
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to update category limit")
                    _uiState.update {
                        it.copy(
                            categoryLimitError = exception.message ?: "Update failed",
                            isUpdatingCategoryLimit = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        categoryLimitError = e.message ?: "Update failed",
                        isUpdatingCategoryLimit = false
                    )
                }
            }
        }
    }

    private fun calculateOverBudgetCategories(categoryLimits: List<CategoryLimitOverview>): List<CategoryLimitOverview> {
        return categoryLimits.filter { limit ->
            (limit.monthlySpent.toDoubleOrNull() ?: 0.0) > (limit.monthlyLimit.toDoubleOrNull() ?: 0.0)
        }
    }

    fun deleteCategoryLimit(categoryId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingCategoryLimit = true) }
            try {
                val result = deleteCategoryLimitUseCase(categoryId)
                if (result.isSuccess) loadCategoryLimits()
            } catch (e: Exception) {
                _uiState.update { it.copy(isDeletingCategoryLimit = false) }
            }
        }
    }

    fun refreshOnScreenFocus() {
        loadAllData()
    }
}

// --- UI STATE MODEL ---

data class GoalScreenState(
    val budgetState: ScreenState<Budget> = ScreenState.Loading,
    val budget: Budget? = null,
    val categoryLimitsState: ScreenState<List<CategoryLimitOverview>> = ScreenState.Loading,
    val overBudgetCategories: List<CategoryLimitOverview> = emptyList(),
    val isNotificationsEnabled: Boolean = true,
    val isUpdating: Boolean = false,
    val isUpdatingCategoryLimit: Boolean = false,
    val isDeletingCategoryLimit: Boolean = false,
    val isCategoryLimitsLoading: Boolean = false,
    val categoryLimitError: String? = null,
    val savingsGoal: SavingsGoal? = null,
    val isSavingsGoalLoading: Boolean = false,
    val isSavingsGoalUpdating: Boolean = false,
    val savingsGoalError: String? = null,
    val monthlyChartData: MonthlyChartData? = null,

    // Interactive Picker States
    val selectedChartMonth: String = "Jan 2026",
    val availableMonths: List<String> = listOf("Jan 2026", "Feb 2026", "Mar 2026", "Apr 2026"),
    val selectedStartDate: Long? = null,
    val selectedEndDate: Long? = null,
    val showDateRangePicker: Boolean = false,

    // Balance Metrics
    val totalBalance: Double = 0.0,
    val balanceComparison: String = "+2.5% vs Last month",

    // NEW: Goals State
    val goalsState: ScreenState<List<Goal>> = ScreenState.Loading,
    val goals: List<Goal> = emptyList()
)