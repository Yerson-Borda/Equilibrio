package com.example.moneymate.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.budget.model.Budget
import com.example.domain.budget.usecase.GetCurrentBudgetUseCase
import com.example.domain.savingsGoal.model.SavingsGoal
import com.example.domain.savingsGoal.usecase.GetCurrentSavingsGoalUseCase
import com.example.domain.transaction.model.SavingsTrendsData
import com.example.domain.transaction.model.TransactionEntity
import com.example.domain.transaction.usecase.GetSavingsTrendsUseCase
import com.example.domain.transaction.usecase.GetTransactionsUseCase
import com.example.domain.user.model.UserDetailedData
import com.example.domain.user.usecase.GetUserDetailedUseCase
import com.example.domain.wallet.model.TotalBalance
import com.example.domain.wallet.usecase.GetTotalBalanceUseCase
import com.example.moneymate.utils.DataSyncManager
import com.example.moneymate.utils.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getUserDetailedUseCase: GetUserDetailedUseCase,
    private val getTotalBalanceUseCase: GetTotalBalanceUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getBudgetUseCase: GetCurrentBudgetUseCase,
    private val getCurrentSavingsGoalUseCase: GetCurrentSavingsGoalUseCase,
    private val getSavingsTrendsUseCase: GetSavingsTrendsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    init {
        println("DEBUG: HomeViewModel init - loading data")
        loadAllData()
        setupDataChangeListener()
    }

    private fun setupDataChangeListener() {
        viewModelScope.launch {
            DataSyncManager.dataChangeEvents.collect { event ->
                when (event) {
                    is DataSyncManager.DataChangeEvent.TransactionsUpdated -> {
                        println("🔄 DEBUG: HomeViewModel - Transaction update detected, refreshing...")
                        refreshTransactionData()
                    }
                    is DataSyncManager.DataChangeEvent.BudgetUpdated -> {
                        println("🔄 DEBUG: HomeViewModel - Budget update detected, refreshing savings...")
                        loadSavingsData()
                    }
                    is DataSyncManager.DataChangeEvent.WalletsUpdated -> {
                        println("🔄 DEBUG: HomeViewModel - Wallet update detected, refreshing balance...")
                        loadTotalBalance()
                    }
                    is DataSyncManager.DataChangeEvent.UserDataUpdated -> {
                        println("🔄 DEBUG: HomeViewModel - User data update detected, refreshing...")
                        loadUserData()
                    }
                    else -> {
                    }
                }
            }
        }
    }
    private fun refreshTransactionData() {
        viewModelScope.launch {
            println("🔄 DEBUG: HomeViewModel - Refreshing transaction data...")
            loadFinancialOverview()
            loadRecentTransactions()
            loadBudgetData()
            loadSavingsData()
        }
    }

    fun loadAllData() {
        loadUserData()
        loadTotalBalance()
        loadFinancialOverview()
        loadRecentTransactions()
        loadBudgetData()
        loadSavingsData()
    }

    fun loadBudgetData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(budgetState = ScreenState.Loading)
            try {
                println("DEBUG: HomeViewModel - Loading budget data...")
                val result = getBudgetUseCase()

                if (result.isSuccess) {
                    val budgetData = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        budgetState = ScreenState.Success(budgetData)
                    )
                    println("DEBUG: HomeViewModel - Loaded budget data: $budgetData")
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to load budget data")
                    _uiState.value = _uiState.value.copy(
                        budgetState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception),
                            retryAction = { loadBudgetData() }
                        )
                    )
                }
            } catch (e: Exception) {
                println("DEBUG: HomeViewModel - Error loading budget data: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    budgetState = ScreenState.Error(
                        com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { loadBudgetData() }
                    )
                )
            }
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(userDataState = ScreenState.Loading)
            try {
                println("DEBUG: HomeViewModel - Loading user data...")
                val data = getUserDetailedUseCase()
                println("DEBUG: HomeViewModel - Loaded user data: $data")
                println("DEBUG: HomeViewModel - User fullName: ${data.user.fullName}")
                println("DEBUG: HomeViewModel - Stats: ${data.stats}")

                _uiState.value = _uiState.value.copy(
                    userDataState = ScreenState.Success(data)
                )
            } catch (e: Exception) {
                println("DEBUG: HomeViewModel - Error loading user data: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    userDataState = ScreenState.Error(
                        com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { loadUserData() }
                    )
                )
            }
        }
    }

    fun loadTotalBalance() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(balanceState = ScreenState.Loading)

            try {
                println("DEBUG: HomeViewModel - Loading total balance...")
                val result = getTotalBalanceUseCase()
                if (result.isSuccess) {
                    val balance = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        balanceState = ScreenState.Success(balance)
                    )
                    println("DEBUG: HomeViewModel - Loaded total balance: $balance")
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to load balance")
                    _uiState.value = _uiState.value.copy(
                        balanceState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception),
                            retryAction = { loadTotalBalance() }
                        )
                    )
                }
            } catch (e: Exception) {
                println("DEBUG: HomeViewModel - Error loading total balance: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    balanceState = ScreenState.Error(
                        com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { loadTotalBalance() }
                    )
                )
            }
        }
    }

    fun loadFinancialOverview() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(financialOverviewState = ScreenState.Loading)
            try {
                println("DEBUG: HomeViewModel - Loading financial overview...")
                val result = getTransactionsUseCase()

                if (result.isSuccess) {
                    val transactions = result.getOrThrow()
                    val (totalIncome, totalExpense) = calculateFinancialTotals(transactions)

                    _uiState.value = _uiState.value.copy(
                        financialOverviewState = ScreenState.Success(
                            FinancialOverviewData(
                                totalIncome = totalIncome,
                                totalExpense = totalExpense
                            )
                        )
                    )
                    println("DEBUG: HomeViewModel - Loaded financial overview: Income=$$totalIncome, Expense=$$totalExpense")
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to load transactions")
                    _uiState.value = _uiState.value.copy(
                        financialOverviewState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception),
                            retryAction = { loadFinancialOverview() }
                        )
                    )
                }
            } catch (e: Exception) {
                println("DEBUG: HomeViewModel - Error loading financial overview: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    financialOverviewState = ScreenState.Error(
                        com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { loadFinancialOverview() }
                    )
                )
            }
        }
    }

    fun loadRecentTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recentTransactionsState = ScreenState.Loading)
            try {
                println("DEBUG: HomeViewModel - Loading recent transactions...")
                val result = getTransactionsUseCase()

                if (result.isSuccess) {
                    val allTransactions = result.getOrThrow()

                    println("DEBUG: All transactions (${allTransactions.size}) loaded:")
                    allTransactions.forEachIndexed { index, transaction ->
                        println("DEBUG: $index: ID ${transaction.id} - '${transaction.name}' - Date: ${transaction.transactionDate}")
                    }
                    val sortedTransactions = allTransactions.sortedWith(
                        compareByDescending<TransactionEntity> {
                            try {
                                java.time.LocalDate.parse(it.transactionDate)
                            } catch (e: Exception) {
                                java.time.LocalDate.MIN
                            }
                        }.thenByDescending { it.id ?: 0 }
                    )
                    val recentTransactions = sortedTransactions.take(5)

                    println("DEBUG: Showing ${recentTransactions.size} recent transactions (sorted newest first):")
                    recentTransactions.forEachIndexed { index, transaction ->
                        println("DEBUG: Recent $index: ID ${transaction.id} - '${transaction.name}' - Date: ${transaction.transactionDate}")
                    }

                    _uiState.value = _uiState.value.copy(
                        recentTransactionsState = ScreenState.Success(recentTransactions)
                    )
                    println("DEBUG: HomeViewModel - Loaded ${recentTransactions.size} recent transactions")
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to load transactions")
                    println("DEBUG: HomeViewModel - Error loading transactions: ${exception.message}")
                    _uiState.value = _uiState.value.copy(
                        recentTransactionsState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception),
                            retryAction = { loadRecentTransactions() }
                        )
                    )
                }
            } catch (e: Exception) {
                println("DEBUG: HomeViewModel - Error loading recent transactions: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    recentTransactionsState = ScreenState.Error(
                        com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { loadRecentTransactions() }
                    )
                )
            }
        }
    }

    fun refreshOnScreenFocus() {
        viewModelScope.launch {
            println("🔄 DEBUG: HomeViewModel - Screen focused, refreshing data...")
            loadAllData()
        }
    }

    private fun calculateFinancialTotals(transactions: List<TransactionEntity>): Pair<Double, Double> {
        var totalIncome = 0.0
        var totalExpense = 0.0

        transactions.forEach { transaction ->
            val amount = try {
                transaction.amount.toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }

            when (transaction.type.lowercase()) {
                "income" -> totalIncome += amount
                "expense" -> totalExpense += amount
            }
        }

        return Pair(totalIncome, totalExpense)
    }

    fun loadSavingsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(savingsGoalState = ScreenState.Loading)
            try {
                println("DEBUG: HomeViewModel - Loading savings goal data...")
                val result = getCurrentSavingsGoalUseCase()
                if (result.isSuccess) {
                    val savingsGoal = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        savingsGoalState = ScreenState.Success(savingsGoal)
                    )
                    println("DEBUG: HomeViewModel - Loaded savings goal: $savingsGoal")
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Failed to load savings goal")
                    _uiState.value = _uiState.value.copy(
                        savingsGoalState = ScreenState.Error(
                            com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception),
                            retryAction = { loadSavingsData() }
                        )
                    )
                }
            } catch (e: Exception) {
                println("DEBUG: HomeViewModel - Error loading savings goal: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    savingsGoalState = ScreenState.Error(
                        com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(e),
                        retryAction = { loadSavingsData() }
                    )
                )
            }
        }
    }
}

data class HomeScreenState(
    val userDataState: ScreenState<UserDetailedData> = ScreenState.Loading,
    val balanceState: ScreenState<TotalBalance?> = ScreenState.Loading,
    val financialOverviewState: ScreenState<FinancialOverviewData> = ScreenState.Loading,
    val recentTransactionsState: ScreenState<List<TransactionEntity>> = ScreenState.Loading,
    val budgetState: ScreenState<Budget?> = ScreenState.Loading,
    val savingsGoalState: ScreenState<SavingsGoal?> = ScreenState.Loading
) {
    val isLoading: Boolean
        get() = userDataState is ScreenState.Loading &&
                balanceState is ScreenState.Loading &&
                financialOverviewState is ScreenState.Loading &&
                budgetState is ScreenState.Loading

    val isTotalBalanceLoading: Boolean
        get() = balanceState is ScreenState.Loading

    val userData: UserDetailedData?
        get() = when (userDataState) {
            is ScreenState.Success -> userDataState.data
            else -> null
        }

    val totalBalance: TotalBalance?
        get() = when (balanceState) {
            is ScreenState.Success -> balanceState.data
            else -> null
        }

    val financialOverview: FinancialOverviewData?
        get() = when (financialOverviewState) {
            is ScreenState.Success -> financialOverviewState.data
            else -> null
        }

    val recentTransactions: List<TransactionEntity>?
        get() = when (recentTransactionsState) {
            is ScreenState.Success -> recentTransactionsState.data
            else -> null
        }

    val budgetData: Budget?
        get() = when (budgetState) {
            is ScreenState.Success -> budgetState.data
            else -> null
        }

    val savingsGoal: SavingsGoal?
        get() = when (savingsGoalState) {
            is ScreenState.Success -> savingsGoalState.data
            else -> null
        }
}

data class FinancialOverviewData(
    val totalIncome: Double,
    val totalExpense: Double
)