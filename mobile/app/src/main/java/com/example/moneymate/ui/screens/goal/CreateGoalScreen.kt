package com.example.moneymate.ui.screens.goal

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.moneymate.utils.Config
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalScreen(
    viewModel: GoalDetailViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Create temporary file for image
    val tempImageFile = remember {
        File.createTempFile(
            "goal_image_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
    }
    
    // Gallery launcher for image selection
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
                    convertUriToFile(context, selectedUri, tempImageFile) { success ->
                        if (success) {
                            viewModel.imagePath = tempImageFile.absolutePath
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "Failed to process selected image",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(
                        context,
                        "Cannot access selected image: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )
    
    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // In a real implementation, you'd get the selected date
                        // For now, we'll just close the dialog
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            // Simple date picker - you can replace with Material 3 DatePicker
            androidx.compose.material3.DatePicker(
                state = androidx.compose.material3.rememberDatePickerState()
            )
        }
    }
    
    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFF8DC6FF))
            ) {
                // Display selected image or placeholder
                if (viewModel.imagePath != null) {
                    AsyncImage(
                        model = Config.buildImageUrl(viewModel.imagePath),
                        contentDescription = "Goal Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Dark overlay for better text contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                } else {
                    // Placeholder icon when no image
                    Icon(
                        Icons.Default.Image, 
                        null, 
                        Modifier
                            .size(64.dp)
                            .align(Alignment.Center), 
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                // Back button
                IconButton(
                    onClick = onBack, 
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.White, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        null,
                        tint = Color.Black
                    )
                }
                
                // Add photo button
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.White, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.AddAPhoto, 
                        null,
                        tint = Color.Black
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                CreateInputField(
                    label = "Title:", 
                    value = viewModel.title, 
                    onValueChange = { viewModel.title = it }
                )

                Spacer(Modifier.height(16.dp))
                
                // Improved deadline picker
                Text(
                    "Goal Deadline:", 
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday, 
                            null,
                            tint = Color(0xFF4A66FF)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            viewModel.deadline.toString(), 
                            color = Color.Black
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                CreateInputField(
                    label = "Goal description:", 
                    value = viewModel.description, 
                    onValueChange = { viewModel.description = it }
                )

                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable autodeposit?")
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = false, 
                        onCheckedChange = {},
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4A66FF)
                        )
                    )
                }

                Spacer(Modifier.height(24.dp))

                CreateInputField(
                    label = "Target Amount:", 
                    value = viewModel.goalAmount, 
                    onValueChange = { viewModel.goalAmount = it }
                )

                Button(
                    onClick = { viewModel.saveGoal { onBack() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A66FF)
                    ),
                    enabled = !viewModel.isSaving
                ) {
                    if (viewModel.isSaving) {
                        CircularProgressIndicator(
                            color = Color.White, 
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Create Goal", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4A66FF),
                unfocusedBorderColor = Color(0xFFE5E7EB)
            )
        )
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