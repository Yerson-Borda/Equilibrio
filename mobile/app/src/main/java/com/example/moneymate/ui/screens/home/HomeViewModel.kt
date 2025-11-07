package com.example.moneymate.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.home.model.HomeData
import com.example.domain.home.usecase.GetHomeDataUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHomeDataUseCase: GetHomeDataUseCase
) : ViewModel() {

    private val _homeData = MutableStateFlow<HomeData?>(null)
    val homeData = _homeData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = getHomeDataUseCase()
                _homeData.value = data
            } catch (e: Exception) {
                _error.emit("Failed to load home data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshHomeData() {
        loadHomeData()
    }
}