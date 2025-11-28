package com.example.moneymate.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.user.model.UserDetailedData
import com.example.domain.user.usecase.GetUserDetailedUseCase
import com.example.domain.wallet.model.TotalBalance
import com.example.domain.wallet.usecase.GetTotalBalanceUseCase
import com.example.moneymate.utils.ScreenState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getUserDetailedUseCase: GetUserDetailedUseCase,
    private val getTotalBalanceUseCase: GetTotalBalanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    init {
        println("DEBUG: HomeViewModel init - loading data")
        loadAllData()
    }

    fun refreshAllData() {
        loadAllData()
    }

    fun loadAllData() {
        loadUserData()
        loadTotalBalance()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(userDataState = ScreenState.Loading)
            delay(5000)

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
}

data class HomeScreenState(
    val userDataState: ScreenState<UserDetailedData> = ScreenState.Loading,
    val balanceState: ScreenState<TotalBalance?> = ScreenState.Loading
) {
    // Helper properties for backward compatibility
    val isLoading: Boolean
        get() = userDataState is ScreenState.Loading && balanceState is ScreenState.Loading

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
}