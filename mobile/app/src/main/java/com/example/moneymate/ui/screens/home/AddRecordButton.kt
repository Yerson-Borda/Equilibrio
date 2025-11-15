package com.example.moneymate.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneymate.R

@Composable
fun AddRecordButton(
    onClick: () -> Unit,
    iconRes: Int,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF4D6BFA),
    size: Int = 48,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

// Preview with different sizes and states
@Preview(showBackground = true)
@Composable
fun AddRecordButtonPreview() {
    androidx.compose.material3.MaterialTheme {
        Box(modifier = Modifier.background(Color(0xFF121212))) {
            AddRecordButton(
                onClick = { },
                iconRes = R.drawable.add_outline,
                contentDescription = "Add",
                size = 48
            )
        }
    }
}