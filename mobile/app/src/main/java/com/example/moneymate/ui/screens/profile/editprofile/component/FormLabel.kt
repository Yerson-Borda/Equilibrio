package com.example.moneymate.ui.screens.profile.editprofile.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFFA2A2A7),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}