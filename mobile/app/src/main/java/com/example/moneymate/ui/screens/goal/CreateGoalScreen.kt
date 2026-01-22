package com.example.moneymate.ui.screens.goal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateGoalScreen(
    viewModel: GoalDetailViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFF8DC6FF))) {
            IconButton(onClick = onBack, Modifier.padding(16.dp).background(Color.White, CircleShape)) {
                Icon(Icons.Default.ArrowBack, null)
            }
            Icon(Icons.Default.Image, null, Modifier.size(48.dp).align(Alignment.Center), tint = Color.White)
            IconButton(onClick = { }, Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.White, CircleShape)) {
                Icon(Icons.Default.AddAPhoto, null)
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            CreateInputField(label = "Title:", value = viewModel.title, onValueChange = { viewModel.title = it })

            Spacer(Modifier.height(16.dp))
            Text("Goal Deadline:", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Icon(Icons.Default.CalendarToday, null)
                Text(viewModel.deadline.toString(), Modifier.padding(start = 12.dp), color = Color.Gray)
            }

            Spacer(Modifier.height(16.dp))
            CreateInputField(label = "Goal description:", value = viewModel.description, onValueChange = { viewModel.description = it })

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable autodeposit?")
                Spacer(Modifier.weight(1f))
                Switch(checked = false, onCheckedChange = {})
            }

            Spacer(Modifier.height(24.dp))

            CreateInputField(label = "Target Amount:", value = viewModel.goalAmount, onValueChange = { viewModel.goalAmount = it })

            Button(
                onClick = { viewModel.saveGoal { onBack() } },
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.isSaving
            ) {
                if (viewModel.isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Create Goal", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CreateInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}