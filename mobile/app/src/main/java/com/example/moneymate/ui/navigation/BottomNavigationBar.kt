package com.example.moneymate.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.R

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onNavigationItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color(0xFFFFFFFF)
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.home),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Home",
                    fontSize = 12.sp,
                    color = if (currentScreen == "home") Color(0xFF4D6BFA) else Color(0xFF666666)
                )
            },
            selected = currentScreen == "home",
            onClick = { onNavigationItemSelected("home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4D6BFA),
                selectedTextColor = Color(0xFF4D6BFA),
                unselectedIconColor = Color(0xFF666666),
                unselectedTextColor = Color(0xFF666666),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_business_chart),
                    contentDescription = "Transactions",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Transactions",
                    fontSize = 11.sp,
                    color = if (currentScreen == "transactions") Color(0xFF4D6BFA) else Color(0xFF666666)
                )
            },
            selected = currentScreen == "transactions",
            onClick = { onNavigationItemSelected("transactions") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4D6BFA),
                selectedTextColor = Color(0xFF4D6BFA),
                unselectedIconColor = Color(0xFF666666),
                unselectedTextColor = Color(0xFF666666),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.wallet),
                    contentDescription = "Wallets",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Wallets",
                    fontSize = 12.sp,
                    color = if (currentScreen == "wallets") Color(0xFF4D6BFA) else Color(0xFF666666)
                )
            },
            selected = currentScreen == "wallets",
            onClick = { onNavigationItemSelected("wallets") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4D6BFA),
                selectedTextColor = Color(0xFF4D6BFA),
                unselectedIconColor = Color(0xFF666666),
                unselectedTextColor = Color(0xFF666666),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_edit_filter),
                    contentDescription = "Goals",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    "Goals",
                    fontSize = 12.sp,
                    color = if (currentScreen == "goals") Color(0xFF4D6BFA) else Color(0xFF666666)
                )
            },
            selected = currentScreen == "goals",
            onClick = { onNavigationItemSelected("goals") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4D6BFA),
                selectedTextColor = Color(0xFF4D6BFA),
                unselectedIconColor = Color(0xFF666666),
                unselectedTextColor = Color(0xFF666666),
                indicatorColor = Color.Transparent
            )
        )
    }
}