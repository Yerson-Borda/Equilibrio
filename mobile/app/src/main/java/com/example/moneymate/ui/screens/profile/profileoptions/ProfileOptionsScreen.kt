package com.example.moneymate.ui.screens.profile.profileoptions

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.moneymate.R
import com.example.moneymate.utils.AppError
import com.example.moneymate.utils.Config
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileOptionsScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onPaymentPreferencesClick: () -> Unit = {},
    onBanksAndCardsClick: () -> Unit = {},
    onMessageCenterClick: () -> Unit = {},
    onExportTransactionsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: ProfileOptionsScreenViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val errorState by viewModel.errorState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
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
            Spacer(modifier = Modifier.width(105.dp))
            Text(
                text = "Profile",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // User Info Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp, start = 25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF333333)),
                contentAlignment = Alignment.Center
            ) {
                uiState.user?.avatarUrl?.let { avatarUrl ->
                    val fullAvatarUrl = Config.buildImageUrl(avatarUrl)
                    AsyncImage(
                        model = fullAvatarUrl,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        placeholder = rememberAsyncImagePainter(
                            model = R.drawable.ic_person
                        ),
                        error = rememberAsyncImagePainter(
                            model = R.drawable.ic_person
                        )
                    )
                } ?: run {
                    // Display user initials if no avatar
                    val initials = uiState.user?.fullName?.let { name ->
                        name.split(" ").take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }
                            .take(2)
                            .uppercase()
                    } ?: "U"

                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(22.dp))

            Column {
                // User Name
                Text(
                    text = uiState.user?.fullName ?: "Loading...",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email
                Text(
                    text = uiState.user?.email ?: "Loading...",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(45.dp))

        // Menu Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            ProfileMenuItem(
                leadingIcon = R.drawable.user_profile_ic,
                title = "Edit Profile",
                onClick = onEditProfileClick,
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(23.dp))
            ProfileMenuItem(
                leadingIcon = R.drawable.cards_ic,
                title = "Payment Preferences",
                onClick = onPaymentPreferencesClick,
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(23.dp))
            ProfileMenuItem(
                leadingIcon = R.drawable.credit_card_ic,
                title = "Banks and Cards",
                onClick = onBanksAndCardsClick,
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(23.dp))
            ProfileMenuItem(
                leadingIcon = R.drawable.chat_message_ic,
                title = "Message Center",
                onClick = onMessageCenterClick,
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(23.dp))
            ProfileMenuItem(
                leadingIcon = R.drawable.location_ic,
                title = "Export transactions",
                onClick = onExportTransactionsClick,
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(23.dp))
            ProfileMenuItem(
                leadingIcon = R.drawable.setting_ic,
                title = "Settings",
                onClick = onSettingsClick,
                enabled = !uiState.isLoading
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit,
    leadingIcon: Int? = null,
    enabled: Boolean = true,
    textColor: Color = Color(0xFF1E1E2D),
    iconTint: Color = Color(0xFFAAAAAA)
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
            if (leadingIcon != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = leadingIcon),
                        contentDescription = null,
                        tint = if (enabled) iconTint else Color.Gray,
                        modifier = Modifier.size(23.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        color = if (enabled) textColor else Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                Text(
                    text = title,
                    color = if (enabled) textColor else Color.Gray,
                    fontSize = 16.sp
                )
            }

            Icon(
                painter = painterResource(R.drawable.arrow),
                contentDescription = "Navigate",
                tint = if (enabled) Color(0xFFAAAAAA) else Color.Gray,
                modifier = Modifier.size(16.dp)
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

@Preview(showBackground = true, backgroundColor = 0xFFF4F4F4)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileOptionsScreen()
    }
}