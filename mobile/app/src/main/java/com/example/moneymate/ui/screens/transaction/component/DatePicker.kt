package com.example.moneymate.ui.screens.transaction.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleMonthPickerButton(
    selectedDateString: String, // Keep as String
    onDateSelected: (String) -> Unit, // Keep as String
    modifier: Modifier = Modifier,
    displayFormat: String = "dd MMM yyyy" // Change default format
) {
    var showDialog by remember { mutableStateOf(false) }

    // Format the date for display - show full date
    val formattedDate = remember(selectedDateString) {
        try {
            val date = LocalDate.parse(selectedDateString)
            DateTimeFormatter.ofPattern(displayFormat).format(date)
        } catch (e: Exception) {
            "Select Date"
        }
    }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(formattedDate)
    }

    if (showDialog) {
        DatePickerDialogMaterial3(
            selectedDateString = selectedDateString,
            onDateSelected = { newDateString ->
                onDateSelected(newDateString)
            },
            onDismiss = { showDialog = false }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogMaterial3(
    selectedDateString: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Convert String to LocalDate for the picker
    val selectedDate = remember(selectedDateString) {
        try {
            LocalDate.parse(selectedDateString)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    val state = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val instant = java.time.Instant.ofEpochMilli(millis)
                        val zoneId = java.time.ZoneId.systemDefault()
                        val selectedLocalDate = java.time.LocalDate.ofInstant(instant, zoneId)
                        // Convert back to String format
                        val selectedDateString = selectedLocalDate.toString()
                        onDateSelected(selectedDateString)
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = state)
    }
}