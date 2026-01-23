package com.example.moneymate.ui.screens.profile.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneymate.R
import com.example.moneymate.utils.AppError
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onDataSharingClick: () -> Unit = {},
    viewModel: SettingsScreenViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val errorState by viewModel.errorState.collectAsStateWithLifecycle()
    val navigationEvent by viewModel.navigationEvent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // State for showing confirmation dialog
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    // Handle errors
    LaunchedEffect(errorState) {
        errorState?.let { error ->
            val message = when (error) {
                is AppError.ValidationError -> error.message
                is AppError.HttpError -> "Server error: ${error.message}"
                is AppError.NetworkError -> "Network error: ${error.message}"
                else -> "Error: ${error.getUserFriendlyMessage()}"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is SettingsNavigationEvent.LogoutSuccess -> {
                onLogout()
                viewModel.clearNavigationEvent()
            }
            else -> {}
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isLoading) {
                    showLogoutConfirmation = false
                }
            },
            title = {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text("Are you sure you want to log out?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutConfirmation = false
                    },
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Log Out")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutConfirmation = false },
                    enabled = !uiState.isLoading
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add system bars padding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
    ) {
        // Header with Back button and Logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(24.dp),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    tint = if (uiState.isLoading) Color.Gray else Color.Black,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Settings",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { showLogoutConfirmation = true },
                modifier = Modifier.size(24.dp),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = "Logout",
                    tint = if (uiState.isLoading) Color.Gray else Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // General Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
        ) {
            // Section Header
            Text(
                text = "General",
                color = Color(0xFFA2A2A7),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Language
            SettingsMenuItem(
                title = "Language",
                subtitle = "English",
                onClick = onLanguageClick,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Notifications
            SettingsMenuItem(
                title = "Notifications",
                onClick = onNotificationsClick,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Contact Us
            SettingsMenuItem(
                title = "Contact Us",
                onClick = onContactUsClick,
                enabled = !uiState.isLoading
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Security Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
        ) {
            // Section Header
            Text(
                text = "Security",
                color = Color(0xFFA2A2A7),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Privacy Policy
            SettingsMenuItem(
                title = "Privacy Policy",
                onClick = onPrivacyPolicyClick,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Data Sharing
            SettingsMenuItem(
                title = "Data Sharing",
                onClick = onDataSharingClick,
                enabled = !uiState.isLoading
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Toggle Options Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
        ) {
            // Biometric Toggle
            SettingsToggleItem(
                title = "Biometric",
                isEnabled = uiState.biometricEnabled,
                onToggleChange = viewModel::updateBiometricSetting,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dark Mode Toggle
            SettingsToggleItem(
                title = "Dark Mode",
                isEnabled = uiState.darkModeEnabled,
                onToggleChange = viewModel::updateDarkModeSetting,
                enabled = !uiState.isLoading
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsMenuItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = if (enabled) Color.Black else Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = Color(0xFFA2A2A7),
                        fontSize = 14.sp
                    )
                }
            }

            Icon(
                painter = painterResource(R.drawable.arrow),
                contentDescription = "Navigate",
                tint = if (enabled) Color(0xFFAAAAAA) else Color.Gray,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFF4F4F4).copy(alpha = 0.3f))
        )
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    isEnabled: Boolean,
    onToggleChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (enabled) Color.Black else Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )

            Switch(
                checked = isEnabled,
                onCheckedChange = { if (enabled) onToggleChange(it) },
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF4361EE),
                    checkedTrackColor = Color(0xFF4361EE).copy(alpha = 0.5f),
                    uncheckedThumbColor = Color(0xFFF1F1F1),
                    uncheckedTrackColor = Color(0xFF939393)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen()
    }
}