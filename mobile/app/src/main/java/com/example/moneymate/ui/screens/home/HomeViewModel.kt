package com.example.moneymate.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.user.model.UserDetailedData
import com.example.domain.user.usecase.GetUserDetailedUseCase
import com.example.domain.wallet.model.TotalBalance
import com.example.domain.wallet.usecase.GetTotalBalanceUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getUserDetailedUseCase: GetUserDetailedUseCase,
    private val getTotalBalanceUseCase: GetTotalBalanceUseCase
) : ViewModel() {

    private val _totalBalance = MutableStateFlow<TotalBalance?>(null)
    val totalBalance: StateFlow<TotalBalance?> = _totalBalance.asStateFlow()

    private val _isTotalBalanceLoading = MutableStateFlow(false)
    val isTotalBalanceLoading: StateFlow<Boolean> = _isTotalBalanceLoading.asStateFlow()

    private val _userData = MutableStateFlow<UserDetailedData?>(null)
    val userData = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        loadUserData()
        loadTotalBalance()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = getUserDetailedUseCase()
                _userData.value = data
            } catch (e: Exception) {
                _error.emit("Failed to load user data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loadTotalBalance() {
        viewModelScope.launch {
            _isTotalBalanceLoading.value = true
            val result = getTotalBalanceUseCase()
            if (result.isSuccess) {
                _totalBalance.value = result.getOrNull()
            }
            _isTotalBalanceLoading.value = false
        }
    }

    fun refreshAllData() {
        loadUserData()
        loadTotalBalance()
    }
}