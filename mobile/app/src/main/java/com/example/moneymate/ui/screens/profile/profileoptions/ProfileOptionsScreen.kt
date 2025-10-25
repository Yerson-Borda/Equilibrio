package com.example.moneymate.ui.screens.profile.profileoptions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.R

@Composable
fun ProfileOptionsScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onPaymentPreferencesClick: () -> Unit = {},
    onBanksAndCardsClick: () -> Unit = {},
    onMessageCenterClick: () -> Unit = {},
    onExportTransactionsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
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
                text = "Profile",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp, start = 25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF333333)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MN",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(22.dp))

            Column {
                // User Name
                Text(
                    text = "Mahfuzul Nabil",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email
                Text(
                    text = "this_is_my_mail@mail.ru",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
            }

        }
        Spacer(modifier = Modifier.height(45.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            ProfileMenuItem(
                leadingIcon = R.drawable.user_profile_ic,
                title = "Edit Profile",
                onClick = onEditProfileClick
            )
            Spacer(modifier = Modifier.height(23.dp))
            ProfileMenuItem(
                leadingIcon = R.drawable.cards_ic,
                title = "Payment Preferences",
                onClick = onPaymentPreferencesClick
            )
            Spacer(modifier = Modifier.height(23.dp))
            ProfileMenuItem(
                leadingIcon = R.drawable.credit_card_ic,
                title = "Banks and Cards",
                onClick = onBanksAndCardsClick
            )
            Spacer(modifier = Modifier.height(23.dp))
            ProfileMenuItem(
                leadingIcon = R.drawable.chat_message_ic,
                title = "Message Center",
                onClick = onMessageCenterClick
            )
            Spacer(modifier = Modifier.height(23.dp))
            ProfileMenuItem(
                leadingIcon = R.drawable.location_ic,
                title = "Export transactions",
                onClick = onExportTransactionsClick
            )
            Spacer(modifier = Modifier.height(23.dp))
            ProfileMenuItem(
                leadingIcon = R.drawable.setting_ic,
                title = "Settings",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit,
    leadingIcon: Int? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(23.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        color = Color(0xFF1E1E2D),
                        fontSize = 16.sp
                    )
                }
            } else {
                Text(
                    text = title,
                    color = Color(0xFF1E1E2D),
                    fontSize = 16.sp
                )
            }

            Icon(
                painter = painterResource(R.drawable.arrow),
                contentDescription = "Navigate",
                tint = Color(0xFFAAAAAA),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier
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