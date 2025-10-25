import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.R

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onDataSharingClick: () -> Unit = {}
) {
    var biometricEnabled by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header with Back button and Logout
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
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Settings",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onLogoutClick,
                modifier = Modifier.size(24.dp)
            ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logout),
                        contentDescription = "Logout part ",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(16.dp)
                    )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // General Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
        ) {
            // Section Header
            Text(
                text = "General",
                color = Color(0xFFA2A2A7),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Language
            SettingsMenuItem(
                title = "Language",
                subtitle = "English",
                onClick = onLanguageClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Notifications
            SettingsMenuItem(
                title = "Notifications",
                onClick = onNotificationsClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Contact Us
            SettingsMenuItem(
                title = "Contact Us",
                onClick = onContactUsClick
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Security Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
        ) {
            // Section Header
            Text(
                text = "Security",
                color = Color(0xFFA2A2A7),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Privacy Policy
            SettingsMenuItem(
                title = "Privacy Policy",
                onClick = onPrivacyPolicyClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Choose what data you share with us",
                color = Color(0xFFA2A2A7)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Toggle Options Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
        ) {
            // Biometric Toggle
            SettingsToggleItem(
                title = "Biometric",
                isEnabled = biometricEnabled,
                onToggleChange = { biometricEnabled = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dark Mode Toggle
            SettingsToggleItem(
                title = "Dark Mode",
                isEnabled = darkModeEnabled,
                onToggleChange = { darkModeEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsMenuItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
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
            Row {
                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.width(180.dp))
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = Color(0xFFA2A2A7),
                        fontSize = 14.sp
                    )
                }
            }

            Icon(
                painter = painterResource(R.drawable.arrow),
                contentDescription = "Navigate",
                tint = Color(0xFFAAAAAA),
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFF4F4F4).copy(alpha = 0.3f))
        )
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    isEnabled: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggleChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF4361EE),
                    checkedTrackColor = Color(0xFF4361EE).copy(alpha = 0.5f),
                    uncheckedThumbColor = Color(0xFFF1F1F1),
                    uncheckedTrackColor = Color(0xFF939393)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen()
    }
}