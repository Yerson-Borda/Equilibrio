// Add this file: com.example.moneymate.ui.screens.home.TopAppBarSection.kt
package com.example.moneymate.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.moneymate.R

@Composable
fun TopAppBarSection(
    userName: String,
    profileImage: String?,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Info with Avatar
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onProfileClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Image
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF333333)),
                contentAlignment = Alignment.Center
            ) {
                if (!profileImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = profileImage,
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
                } else {
                    // Display user initials if no avatar
                    val initials = userName.split(" ")
                        .take(2)
                        .joinToString("") { it.firstOrNull()?.toString() ?: "" }
                        .take(2)
                        .uppercase()

                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User Greeting
            Column {
                Text(
                    text = "Hello,",
                    color = Color(0xFF7E848D),
                    fontSize = 14.sp
                )
                Text(
                    text = userName,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Notification Icon
        IconButton(
            onClick = { /* Handle notification click */ }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = "Notifications",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}