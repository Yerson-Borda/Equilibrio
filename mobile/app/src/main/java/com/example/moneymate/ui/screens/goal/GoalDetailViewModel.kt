package com.example.moneymate.ui.screens.goal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.goal.model.Goal
import com.example.domain.goal.usecase.*
import com.example.moneymate.utils.AppError
import com.example.moneymate.utils.DataSyncManager
import com.example.moneymate.utils.ScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class GoalDetailViewModel(
    private val getGoalUseCase: GetGoalUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScreenState<Goal>>(ScreenState.Loading)
    val uiState: StateFlow<ScreenState<Goal>> = _uiState.asStateFlow()

    // Internal ID to track if we are editing or creating
    private var currentGoalId: Int? = null

    // Form States
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var goalAmount by mutableStateOf("")
    var deadline by mutableStateOf<LocalDate>(LocalDate.now().plusYears(1))
    var imagePath by mutableStateOf<String?>(null)
    var isSaving by mutableStateOf(false)

    /**
     * Called from the UI/Navigation to start the screen logic
     */
    fun initialize(id: Int?) {
        if (id == null || id == 0) {
            currentGoalId = null
            _uiState.value = ScreenState.Empty // Mode: Create
        } else {
            currentGoalId = id
            loadGoal(id) // Mode: Detail/Edit
        }
    }

    private fun loadGoal(id: Int) {
        viewModelScope.launch {
            _uiState.value = ScreenState.Loading
            val result = getGoalUseCase(id)
            if (result.isSuccess) {
                val goal = result.getOrThrow()
                _uiState.value = ScreenState.Success(goal)
                // Pre-fill fields
                title = goal.title
                description = goal.description ?: ""
                goalAmount = goal.goalAmount.toString()
                deadline = goal.deadline ?: LocalDate.now()
            } else {
                val exception = result.exceptionOrNull()
                _uiState.value = ScreenState.Error(
                    error = com.example.moneymate.utils.ErrorHandler.mapExceptionToAppError(exception ?: Exception("Failed")),
                    retryAction = { loadGoal(id) }
                )
            }
        }
    }

    fun saveGoal(onSuccess: () -> Unit) {
        val amount = goalAmount.toDoubleOrNull() ?: 0.0
        isSaving = true
        viewModelScope.launch {
            val result = if (currentGoalId == null) {
                createGoalUseCase(
                    title = title,
                    goalAmount = amount,
                    currency = "USD",
                    description = description,
                    deadline = deadline,
                    imagePath = imagePath
                )
            } else {
                updateGoalUseCase(
                    goalId = currentGoalId!!,
                    title = title,
                    description = description,
                    deadline = deadline,
                    goalAmount = amount
                )
            }

            if (result.isSuccess) {
                DataSyncManager.notifyGoalsUpdated()
                onSuccess()
            } else {
                isSaving = false
            }
        }
    }
}