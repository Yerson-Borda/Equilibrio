package com.example.moneymate.ui.screens.profile.editprofile

import android.content.Intent
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
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.moneymate.R
import com.example.moneymate.ui.components.CurrencyDropdown
import com.example.moneymate.ui.screens.profile.editprofile.component.CustomEditTextField
import com.example.moneymate.ui.screens.profile.editprofile.component.FormLabel
import com.example.moneymate.utils.AppError
import com.example.moneymate.utils.Config
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream

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

    // Track if we're specifically uploading an avatar
    var isUploadingAvatar by remember { mutableStateOf(false) }

    // Create a temporary file for storing the selected image
    val tempAvatarFile = remember {
        File.createTempFile(
            "avatar_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
    }

    // Add gallery launcher for avatar selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { selectedUri ->
                try {
                    // Take persistable URI permission
                    context.contentResolver.takePersistableUriPermission(
                        selectedUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    // Convert content URI to file path
                    isUploadingAvatar = true
                    convertUriToFile(context, selectedUri, tempAvatarFile) { success ->
                        if (success) {
                            viewModel.uploadAvatar(tempAvatarFile.absolutePath)
                        } else {
                            isUploadingAvatar = false
                            android.widget.Toast.makeText(
                                context,
                                "Failed to process selected image",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    isUploadingAvatar = false
                    android.widget.Toast.makeText(
                        context,
                        "Cannot access selected image: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    // Reset uploading state when loading completes
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isUploadingAvatar = false
        }
    }

    // Handle errors
    LaunchedEffect(errorState) {
        errorState?.let { error ->
            val message = when (error) {
                is AppError.ValidationError -> error.message
                is AppError.HttpError -> "Server error: ${error.message}"
                is AppError.NetworkError -> "Network error: ${error.message}"
                else -> "Error: ${error.getUserFriendlyMessage()}"
            }
            android.widget.Toast.makeText(
                context,
                message,
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
                // Avatar with loading state
                if (isUploadingAvatar) {
                    // Show loading indicator specifically for avatar upload
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333333)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                strokeWidth = 3.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Uploading...",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Normal clickable avatar
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF333333))
                            .clickable(
                                enabled = !uiState.isLoading,
                                onClick = {
                                    // Open gallery to select new avatar
                                    galleryLauncher.launch("image/*")
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Show avatar image if available, otherwise show initials
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
                            } ?: "U"

                            Text(
                                text = initials,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // "Change Photo" text (also clickable)
                Spacer(modifier = Modifier.height(8.dp))
                if (!isUploadingAvatar) {
                    Text(
                        text = "Change Photo",
                        color = if (uiState.isLoading) Color.Gray else Color(0xFF4D6BFA),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(
                            enabled = !uiState.isLoading,
                            onClick = {
                                galleryLauncher.launch("image/*")
                            }
                        )
                    )
                }

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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Email Address
                FormLabel(text = "Email Address")
                CustomEditTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    placeholder = "Enter your email",
                    leadingIcon = R.drawable.ic_email,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Phone Number and Currency Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Phone Number - 60% width
                    Column(
                        modifier = Modifier.weight(0.6f)
                    ) {
                        FormLabel(text = "Phone Number")
                        CustomEditTextField(
                            value = uiState.phoneNumber,
                            onValueChange = viewModel::updatePhoneNumber,
                            placeholder = "Enter your phone number",
                            leadingIcon = R.drawable.ic_phone,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        )
                    }

                    // Display Currency - 40% width
                    Column(
                        modifier = Modifier.weight(0.4f)
                    ) {
                        FormLabel(text = "Display Currency")
                        CurrencyDropdown(
                            selectedCurrency = uiState.defaultCurrency,
                            onCurrencySelected = viewModel::updateDefaultCurrency,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }


                Spacer(modifier = Modifier.height(20.dp))

                // Birth Date
                FormLabel(text = "Birth Date")
                CustomEditTextField(
                    value = uiState.birthDate,
                    onValueChange = viewModel::updateBirthDate,
                    placeholder = "Enter your birth date",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
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
                    if (uiState.isLoading && !isUploadingAvatar) {
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

                // Show avatar upload status
                if (isUploadingAvatar) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Uploading avatar...",
                        color = Color(0xFF4D6BFA),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

// Helper function to convert content URI to file
private fun convertUriToFile(
    context: android.content.Context,
    uri: android.net.Uri,
    outputFile: File,
    onComplete: (Boolean) -> Unit
) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        onComplete(true)
    } catch (e: Exception) {
        e.printStackTrace()
        onComplete(false)
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    MaterialTheme {
        EditProfileScreen()
    }
}