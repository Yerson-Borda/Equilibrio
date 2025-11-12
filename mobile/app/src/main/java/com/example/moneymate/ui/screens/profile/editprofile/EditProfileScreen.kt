package com.example.moneymate.ui.screens.profile.editprofile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.moneymate.R
import com.example.moneymate.ui.screens.profile.editprofile.component.CustomEditTextField
import com.example.moneymate.ui.screens.profile.editprofile.component.FormLabel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
    onUpdateClick: () -> Unit = {},
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val errorState by viewModel.errorState.collectAsStateWithLifecycle()
    val navigationEvent by viewModel.navigationEvent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Add gallery launcher for avatar selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.uploadAvatar(it.toString())
            }
        }
    )

    // Handle errors
    LaunchedEffect(errorState) {
        errorState?.let { error ->
            android.widget.Toast.makeText(
                context,
                error.getUserFriendlyMessage(),
                android.widget.Toast.LENGTH_LONG
            ).show()
            viewModel.clearError()
        }
    }

    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.ProfileUpdated -> {
                onUpdateClick()
                viewModel.clearNavigationEvent()
            }
            else -> {}
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_arrow),
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(modifier = Modifier.width(105.dp))
                Text(
                    text = "Edit Profile",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Info Section with Clickable Avatar
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Clickable Avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF333333))
                        .clickable {
                            // Open gallery to select new avatar
                            galleryLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Display user initials
                    val initials = uiState.user?.fullName?.let { name ->
                        name.split(" ").take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }
                    } ?: "U"

                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // "Change Photo" text (also clickable)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Change Photo",
                    color = Color(0xFF4D6BFA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        galleryLauncher.launch("image/*")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                        color = Color(0xFF7E848D),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
            ) {
                // Full Name
                FormLabel(text = "Full Name")
                CustomEditTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::updateFullName,
                    placeholder = "Enter your full name",
                    leadingIcon = R.drawable.ic_person,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Email Address
                FormLabel(text = "Email Address")
                CustomEditTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    placeholder = "Enter your email",
                    leadingIcon = R.drawable.ic_email,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Phone Number
                FormLabel(text = "Phone Number")
                CustomEditTextField(
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::updatePhoneNumber,
                    placeholder = "Enter your phone number",
                    leadingIcon = R.drawable.ic_phone,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Birth Date
                FormLabel(text = "Birth Date")
                CustomEditTextField(
                    value = uiState.birthDate,
                    onValueChange = viewModel::updateBirthDate,
                    placeholder = "Enter your birth date",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // New Password
                FormLabel(text = "New Password")
                CustomEditTextField(
                    value = uiState.newPassword,
                    onValueChange = viewModel::updateNewPassword,
                    placeholder = "......",
                    isPassword = true,
                    leadingIcon = R.drawable.ic_lock,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm Password
                FormLabel(text = "Confirm password")
                CustomEditTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    placeholder = "......",
                    isPassword = true,
                    leadingIcon = R.drawable.ic_lock,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Update Button
                Button(
                    onClick = { viewModel.updateProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4D6BFA)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !uiState.isLoading && uiState.hasChanges
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Update",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    MaterialTheme {
        EditProfileScreen()
    }
}