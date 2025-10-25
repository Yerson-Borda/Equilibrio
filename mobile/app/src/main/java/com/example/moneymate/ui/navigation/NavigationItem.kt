package com.example.langswap.ui.navigation

// ui/navigation/NavigationItem.kt
sealed class NavigationItem(val route: String) {
    object Splash : NavigationItem("splash")
    object Start : NavigationItem("start")
    object SignUp : NavigationItem("sign_up")
    object SignIn : NavigationItem("sign_in")
    object  Home : NavigationItem("Home")
    object Transactions: NavigationItem("Transactions")
    object Wallets: NavigationItem("Wallets")
    object Goals: NavigationItem("Goals")
    object Profile: NavigationItem("Profile")
    object EditProfile: NavigationItem("EditProfile")
    object Settings: NavigationItem("Settings")

}
