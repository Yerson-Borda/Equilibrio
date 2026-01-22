package com.example.moneymate.ui.components.states

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.moneymate.utils.ScreenState

@Composable
fun <T> StateManager(
    state: ScreenState<T>,
    onRetry: (() -> Unit)? = null,
    loadingMessage: String = "Loading...",
    emptyTitle: String = "No data",
    emptyMessage: String = "There's nothing to display here",
    emptyIcon: ImageVector? = null,
    emptyAction: (@Composable () -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    when (state) {
        is ScreenState.Loading -> {
            FullScreenLoading(message = loadingMessage)
        }
        is ScreenState.Error -> {
            FullScreenError(
                error = state.error,
                onRetry = onRetry ?: state.retryAction
            )
        }
        is ScreenState.Empty -> {
            EmptyState(
                title = emptyTitle,
                message = emptyMessage,
                icon = emptyIcon
            )
        }
        is ScreenState.Success -> {
            content(state.data)
        }
    }
}

@Composable
fun <T> SectionStateManager(
    state: ScreenState<T>,
    onRetry: (() -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    when (state) {
        is ScreenState.Loading -> {
            SectionLoading()
        }
        is ScreenState.Error -> {
            SectionError(
                error = state.error,
                onRetry = onRetry
            )
        }
        is ScreenState.Empty -> {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp))
        }
        is ScreenState.Success -> {
            content(state.data)
        }
    }
}