package com.example.moneymate.ui.screens.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.budget.model.Budget
import com.example.domain.budget.usecase.GetCurrentBudgetUseCase
import com.example.domain.budget.usecase.UpdateBudgetUseCase
import com.example.moneymate.utils.DataSyncManager
import com.example.moneymate.utils.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalScreenViewModel(
    private val getCurrentBudgetUseCase: GetCurrentBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalScreenState())
    val uiState: StateFlow<GoalScreenState> = _uiState.asStateFlow()

    init {
        println("DEBUG: GoalScreenViewModel init - loading budget data")
        loadBudgetData()
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
            println("🔄 DEBUG: GoalScreenViewModel - Screen focused, refreshing budget data...")
            loadBudgetData()
        }
    }
}

data class GoalScreenState(
    val budgetState: ScreenState<Budget> = ScreenState.Loading,
    val isNotificationsEnabled: Boolean = true,
    val currentTabIndex: Int = 0,
    val isUpdating: Boolean = false
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
}