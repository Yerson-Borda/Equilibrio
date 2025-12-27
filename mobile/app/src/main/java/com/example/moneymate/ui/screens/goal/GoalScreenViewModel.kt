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
    private val deleteCategoryLimitUseCase: DeleteCategoryLimitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalScreenState())
    val uiState: StateFlow<GoalScreenState> = _uiState.asStateFlow()

    init {
        println("DEBUG: GoalScreenViewModel init - loading budget data")
        loadBudgetData()
        loadCategoryLimits()
        setupDataChangeListener()
    }

    private fun setupDataChangeListener() {
        viewModelScope.launch {
            DataSyncManager.dataChangeEvents.collect { event ->
                when (event) {
                    is DataSyncManager.DataChangeEvent.BudgetUpdated -> {
                        println("🔄 DEBUG: GoalScreenViewModel - Budget update detected, refreshing...")
                        loadBudgetData()
                    }
                    is DataSyncManager.DataChangeEvent.TransactionsUpdated -> {
                        println("🔄 DEBUG: GoalScreenViewModel - Transaction update detected, refreshing budget...")
                        loadBudgetData()
                    }
                    // Add this if you added CategoryLimitsUpdated to DataSyncManager
                    DataSyncManager.DataChangeEvent.CategoryLimitsUpdated -> {
                        println("🔄 DEBUG: GoalScreenViewModel - Category limits update detected, refreshing...")
                        loadCategoryLimits()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    fun loadBudgetData() {
        viewModelScope.launch {
            println("DEBUG: GoalScreenViewModel - Starting to load budget data")
            _uiState.update { it.copy(budgetState = ScreenState.Loading) }

            try {
                val result = getCurrentBudgetUseCase()
                println("DEBUG: GoalScreenViewModel - UseCase result: $result")

                if (result.isSuccess) {
                    val budgetData = result.getOrThrow()
                    println("DEBUG: GoalScreenViewModel - Budget data loaded: $budgetData")

                    _uiState.update {
                        it.copy(
                            budgetState = ScreenState.Success(budgetData)
                        )
                    }
                    println("DEBUG: GoalScreenViewModel - UI State updated to Success")
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to load budget data")
                    println("DEBUG: GoalScreenViewModel - UseCase failed: ${exception.message}")

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
                println("DEBUG: GoalScreenViewModel - Exception in loadBudgetData: ${e.message}")
                e.printStackTrace()

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

    // GoalScreenViewModel.kt
    fun updateCategoryLimit(categoryId: Int, monthlyLimit: String) {
        viewModelScope.launch {
            println("DEBUG: GoalScreenViewModel - Updating category limit for category $categoryId to $monthlyLimit")
            _uiState.update { it.copy(isUpdatingCategoryLimit = true) }

            try {
                val result = updateCategoryLimitUseCase(categoryId, monthlyLimit)
                println("DEBUG: GoalScreenViewModel - Update category limit result: $result")

                if (result.isSuccess) {
                    // Refresh the category limits list
                    loadCategoryLimits()
                    println("DEBUG: GoalScreenViewModel - Category limit updated successfully")
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to update category limit")
                    println("DEBUG: GoalScreenViewModel - Update failed: ${exception.message}")
                    _uiState.update {
                        it.copy(
                            categoryLimitError = exception.message ?: "Update failed",
                            isUpdatingCategoryLimit = false // Reset loading state
                        )
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: GoalScreenViewModel - Error updating category limit: ${e.message}")
                _uiState.update {
                    it.copy(
                        categoryLimitError = e.message ?: "Update failed",
                        isUpdatingCategoryLimit = false // Reset loading state
                    )
                }
            }
        }
    }

    fun loadCategoryLimits() {
        viewModelScope.launch {
            println("DEBUG: GoalScreenViewModel - Loading category limits")
            _uiState.update {
                it.copy(
                    categoryLimitsState = ScreenState.Loading,
                    isCategoryLimitsLoading = true
                )
            }

            try {
                val result = getCategoryLimitsUseCase()
                println("DEBUG: GoalScreenViewModel - Category limits result: $result")

                if (result.isSuccess) {
                    val categoryLimits = result.getOrThrow()
                    println("DEBUG: GoalScreenViewModel - Category limits loaded: ${categoryLimits.size} items")

                    // Calculate over-budget categories locally
                    val overBudgetCategories = calculateOverBudgetCategories(categoryLimits)

                    _uiState.update {
                        it.copy(
                            categoryLimitsState = ScreenState.Success(categoryLimits),
                            overBudgetCategories = overBudgetCategories,
                            isCategoryLimitsLoading = false,
                            isUpdatingCategoryLimit = false, // Reset update loading state
                            isDeletingCategoryLimit = false  // Reset delete loading state
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to load category limits")
                    println("DEBUG: GoalScreenViewModel - Category limits failed: ${exception.message}")

                    _uiState.update {
                        it.copy(
                            categoryLimitsState = ScreenState.Error(
                                com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception),
                                retryAction = { loadCategoryLimits() }
                            ),
                            isCategoryLimitsLoading = false,
                            isUpdatingCategoryLimit = false, // Reset update loading state
                            isDeletingCategoryLimit = false  // Reset delete loading state
                        )
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: GoalScreenViewModel - Exception in loadCategoryLimits: ${e.message}")
                e.printStackTrace()

                _uiState.update {
                    it.copy(
                        categoryLimitsState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                            retryAction = { loadCategoryLimits() }
                        ),
                        isCategoryLimitsLoading = false,
                        isUpdatingCategoryLimit = false, // Reset update loading state
                        isDeletingCategoryLimit = false  // Reset delete loading state
                    )
                }
            }
        }
    }

    // Helper function to calculate over-budget categories
    private fun calculateOverBudgetCategories(categoryLimits: List<CategoryLimitOverview>): List<CategoryLimitOverview> {
        return categoryLimits.filter { limit ->
            try {
                val limitAmount = limit.monthlyLimit.toDoubleOrNull() ?: 0.0
                val spentAmount = limit.monthlySpent.toDoubleOrNull() ?: 0.0
                spentAmount > limitAmount
            } catch (e: Exception) {
                false
            }
        }
    }

    // Helper function to calculate remaining budget for a category
    private fun calculateRemainingBudget(limit: CategoryLimitOverview): Double {
        return try {
            val limitAmount = limit.monthlyLimit.toDoubleOrNull() ?: 0.0
            val spentAmount = limit.monthlySpent.toDoubleOrNull() ?: 0.0
            limitAmount - spentAmount
        } catch (e: Exception) {
            0.0
        }
    }
    fun deleteCategoryLimit(categoryId: Int) {
        viewModelScope.launch {
            println("DEBUG: GoalScreenViewModel - Deleting category limit for category $categoryId")
            _uiState.update { it.copy(isDeletingCategoryLimit = true) }

            try {
                val result = deleteCategoryLimitUseCase(categoryId)
                println("DEBUG: GoalScreenViewModel - Delete category limit result: $result")

                if (result.isSuccess) {
                    // Update the current list by removing the deleted item
                    val currentLimits = _uiState.value.categoryLimitsState
                    if (currentLimits is ScreenState.Success) {
                        val updatedList = currentLimits.data.filter { it.categoryId != categoryId }
                        val updatedOverBudget = calculateOverBudgetCategories(updatedList)
                        _uiState.update {
                            it.copy(
                                categoryLimitsState = ScreenState.Success(updatedList),
                                overBudgetCategories = updatedOverBudget,
                                isDeletingCategoryLimit = false
                            )
                        }
                    }
                    // If you added CategoryLimitsUpdated to DataSyncManager, uncomment this:
                    // DataSyncManager.notifyCategoryLimitsUpdated()
                    println("DEBUG: GoalScreenViewModel - Category limit deleted successfully")
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to delete category limit")
                    _uiState.update {
                        it.copy(
                            categoryLimitError = exception.message,
                            isDeletingCategoryLimit = false
                        )
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: GoalScreenViewModel - Error deleting category limit: ${e.message}")
                _uiState.update {
                    it.copy(
                        categoryLimitError = e.message,
                        isDeletingCategoryLimit = false
                    )
                }
            }

            // Clear error after a delay
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _uiState.update { it.copy(categoryLimitError = null) }
            }
        }
    }

    fun updateBudget(monthlyLimit: Double?, dailyLimit: Double?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            try {
                println("DEBUG: GoalScreenViewModel - Updating budget...")
                val result = updateBudgetUseCase(monthlyLimit, dailyLimit)

                if (result.isSuccess) {
                    val updatedBudget = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        budgetState = ScreenState.Success(updatedBudget),
                        isUpdating = false
                    )
                    println("DEBUG: GoalScreenViewModel - Budget updated successfully: $updatedBudget")
                    DataSyncManager.notifyBudgetUpdated()
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to update budget")
                    _uiState.value = _uiState.value.copy(
                        budgetState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception),
                            retryAction = { updateBudget(monthlyLimit, dailyLimit) }
                        ),
                        isUpdating = false
                    )
                }
            } catch (e: Exception) {
                println("DEBUG: GoalScreenViewModel - Error updating budget: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    budgetState = ScreenState.Error(
                        com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { updateBudget(monthlyLimit, dailyLimit) }
                    ),
                    isUpdating = false
                )
            }
        }
    }

    fun toggleNotifications() {
        _uiState.update { it.copy(isNotificationsEnabled = !it.isNotificationsEnabled) }
    }

    fun setCurrentTab(index: Int) {
        _uiState.update { it.copy(currentTabIndex = index) }
    }

    fun refreshOnScreenFocus() {
        viewModelScope.launch {
            println("🔄 DEBUG: GoalScreenViewModel - Screen focused, refreshing data...")
            loadBudgetData()
            loadCategoryLimits()
        }
    }

    fun clearCategoryLimitError() {
        _uiState.update { it.copy(categoryLimitError = null) }
    }
}

data class GoalScreenState(
    val budgetState: ScreenState<Budget> = ScreenState.Loading,
    val categoryLimitsState: ScreenState<List<CategoryLimitOverview>> = ScreenState.Loading,
    val overBudgetCategories: List<CategoryLimitOverview> = emptyList(),
    val isNotificationsEnabled: Boolean = true,
    val currentTabIndex: Int = 0,
    val isUpdating: Boolean = false,
    val isUpdatingCategoryLimit: Boolean = false,
    val isDeletingCategoryLimit: Boolean = false,
    val isCategoryLimitsLoading: Boolean = false,
    val categoryLimitError: String? = null
) {
    val isLoading: Boolean
        get() = budgetState is ScreenState.Loading

    val budget: Budget?
        get() = when (budgetState) {
            is ScreenState.Success -> budgetState.data
            else -> null
        }

    val error: com.example.moneymate.utils.AppError?
        get() = when (budgetState) {
            is ScreenState.Error -> budgetState.error
            else -> null
        }

    val categoryLimits: List<CategoryLimitOverview>
        get() = when (categoryLimitsState) {
            is ScreenState.Success -> categoryLimitsState.data
            else -> emptyList()
        }
}